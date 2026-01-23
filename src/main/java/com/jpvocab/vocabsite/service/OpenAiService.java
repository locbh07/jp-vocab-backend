package com.jpvocab.vocabsite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class OpenAiService {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public JsonNode suggest(Vocabulary vocab, String mode, String language, String notes) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }

        String prompt = buildPrompt(vocab, mode, language, notes);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", MODEL);
        payload.put("temperature", 0.2);
        payload.put("response_format", Collections.singletonMap("type", "json_object"));

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("system",
                "You are a Japanese vocabulary assistant. " +
                "Return ONLY valid JSON with the required schema. Do not add extra text."));
        messages.add(message("user", prompt));
        payload.put("messages", messages);

        String content = requestChatCompletion(payload, apiKey);
        try {
            return mapper.readTree(content);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenAI returned non-JSON content");
        }
    }

    public JsonNode suggestBulk(List<Vocabulary> vocabList,
                                String mode,
                                List<String> fields,
                                boolean strictJson) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }

        // Build a JSON-only prompt to keep responses machine-readable.
        String prompt = buildBulkPrompt(vocabList, mode, fields, strictJson);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", MODEL);
        payload.put("temperature", 0.2);
        if (strictJson) {
            payload.put("response_format", Collections.singletonMap("type", "json_object"));
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message("system",
                "You are a Japanese vocabulary assistant. " +
                "Return ONLY valid JSON with the required schema. Do not add extra text."));
        messages.add(message("user", prompt));
        payload.put("messages", messages);

        String content = requestChatCompletion(payload, apiKey);
        try {
            return mapper.readTree(content);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenAI returned non-JSON content");
        }
    }

    public Map<String, Object> convertJsonToMap(JsonNode node) {
        if (node == null || !node.isObject()) {
            return Collections.emptyMap();
        }
        return mapper.convertValue(node, Map.class);
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private String requestChatCompletion(Map<String, Object> payload,
                                         String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Map response = postWithRetry(entity);
        if (response == null) {
            throw new IllegalStateException("Empty OpenAI response");
        }

        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List) || ((List) choicesObj).isEmpty()) {
            throw new IllegalStateException("Invalid OpenAI response");
        }
        Object first = ((List) choicesObj).get(0);
        if (!(first instanceof Map)) {
            throw new IllegalStateException("Invalid OpenAI response");
        }
        Object messageObj = ((Map) first).get("message");
        if (!(messageObj instanceof Map)) {
            throw new IllegalStateException("Invalid OpenAI response");
        }
        Object contentObj = ((Map) messageObj).get("content");
        if (!(contentObj instanceof String)) {
            throw new IllegalStateException("Invalid OpenAI response");
        }
        return (String) contentObj;
    }

    private Map postWithRetry(HttpEntity<Map<String, Object>> entity) {
        int attempts = 0;
        long backoffMs = 500L;
        while (true) {
            try {
                return restTemplate.postForObject(API_URL, entity, Map.class);
            } catch (HttpStatusCodeException ex) {
                HttpStatus status = ex.getStatusCode();
                if (shouldRetry(status) && attempts < 2) {
                    sleep(backoffMs);
                    attempts++;
                    backoffMs *= 2;
                    continue;
                }
                throw new IllegalStateException("OpenAI request failed: HTTP " + status.value());
            } catch (RestClientException ex) {
                if (attempts < 2) {
                    sleep(backoffMs);
                    attempts++;
                    backoffMs *= 2;
                    continue;
                }
                throw new IllegalStateException("OpenAI request failed: " + ex.getMessage());
            }
        }
    }

    private boolean shouldRetry(HttpStatus status) {
        return status == HttpStatus.TOO_MANY_REQUESTS || status.is5xxServerError();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildPrompt(Vocabulary vocab, String mode, String language, String notes) {
        Map<String, Object> v = new LinkedHashMap<>();
        v.put("word_ja", vocab.getWord_ja());
        v.put("word_hira_kana", vocab.getWord_hira_kana());
        v.put("word_romaji", vocab.getWord_romaji());
        v.put("word_vi", vocab.getWord_vi());
        v.put("example_ja", vocab.getExample_ja());
        v.put("example_vi", vocab.getExample_vi());
        v.put("topic", vocab.getTopic());
        v.put("level", vocab.getLevel());
        v.put("image_url", vocab.getImage_url());
        v.put("audio_url", vocab.getAudio_url());
        v.put("core_order", vocab.getCore_order());

        String vocabJson;
        try {
            vocabJson = mapper.writeValueAsString(v);
        } catch (Exception ex) {
            vocabJson = "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Task: Suggest fixes or enrichments for a Japanese vocabulary entry.\n");
        sb.append("Mode: ").append(mode).append("\n");
        sb.append("Language: ").append(language).append("\n");
        if (notes != null && !notes.trim().isEmpty()) {
            sb.append("Notes: ").append(notes.trim()).append("\n");
        }
        sb.append("Input vocabulary JSON:\n");
        sb.append(vocabJson).append("\n");
        sb.append("Return ONLY valid JSON with this exact shape:\n");
        sb.append("{\"suggested\":{");
        sb.append("\"word_ja\":\"(optional, kanji if confident)\",");
        sb.append("\"word_hira_kana\":\"(optional)\",");
        sb.append("\"word_romaji\":\"(optional)\",");
        sb.append("\"word_vi\":\"(optional)\",");
        sb.append("\"example_ja\":\"(optional)\",");
        sb.append("\"example_vi\":\"(optional)\",");
        sb.append("\"topic\":\"(optional)\",");
        sb.append("\"level\":\"(optional)\",");
        sb.append("\"image_url\":\"(optional or empty)\",");
        sb.append("\"audio_url\":\"(optional or empty)\",");
        sb.append("\"imageQuery\":\"(optional)\",");
        sb.append("\"audioQuery\":\"(optional)\",");
        sb.append("\"notes\":\"explain what you changed and why\"");
        sb.append("}}\n");
        sb.append("Rules: Do not scrape the web. ");
        sb.append("Do NOT provide direct image/audio URLs, only imageQuery/audioQuery hints if needed. ");
        sb.append("Only suggest imageQuery/audioQuery if image_url or audio_url is missing or looks invalid. ");
        sb.append("Do not include fields you are not changing, except notes. ");
        sb.append("If example_ja contains <ruby> tags, preserve the ruby markup and only fix typos/grammar. ");
        sb.append("Only change ruby readings (<rt>) when the context clearly requires it; otherwise keep existing readings.");
        sb.append("If word_ja is kana-only and you are confident of the kanji form, suggest word_ja; otherwise keep it. ");
        sb.append("When choosing kanji for homophones (e.g., ひとり), decide based on word_vi/example_ja/meaning; avoid defaulting to less common forms.");
        return sb.toString();
    }

    private String buildBulkPrompt(List<Vocabulary> vocabList,
                                   String mode,
                                   List<String> fields,
                                   boolean strictJson) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Vocabulary vocab : vocabList) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", vocab.getId());
            row.put("word_ja", vocab.getWord_ja());
            row.put("word_hira_kana", vocab.getWord_hira_kana());
            row.put("word_romaji", vocab.getWord_romaji());
            row.put("word_vi", vocab.getWord_vi());
            row.put("example_ja", vocab.getExample_ja());
            row.put("example_vi", vocab.getExample_vi());
            row.put("topic", vocab.getTopic());
            row.put("level", vocab.getLevel());
            items.add(row);
        }

        String itemsJson;
        try {
            itemsJson = mapper.writeValueAsString(items);
        } catch (Exception ex) {
            itemsJson = "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Task: Suggest fixes or enrichments for Japanese vocabulary entries.\n");
        sb.append("Mode: ").append(mode).append("\n");
        sb.append("Fields: ").append(fields).append("\n");
        sb.append("Rules:\n");
        sb.append("- Return ONLY valid JSON.\n");
        sb.append("- Do not hallucinate; if unsure, omit the field or set it to null.\n");
        sb.append("- Do not change word_ja unless mode=enrich and there is a clear error.\n");
        sb.append("- If the entry has only kana and no kanji, propose kanji if confident; otherwise leave word_ja unchanged.\n");
        sb.append("- Only include fields listed in Fields (plus word_ja only if allowed by the rule above).\n");
        sb.append("- If example_ja contains <ruby> tags, keep the ruby markup and only fix typos/grammar.\n");
        sb.append("- Only change ruby readings (<rt>) when the context clearly requires it; otherwise keep existing readings.\n");
        if (strictJson) {
            sb.append("- Output must be strict JSON with no extra text.\n");
        }
        sb.append("Input items (array of objects):\n");
        sb.append(itemsJson).append("\n");
        sb.append("Return ONLY JSON with this shape:\n");
        sb.append("{\"items\":[");
        sb.append("{\"id\":123,");
        sb.append("\"suggested\":{");
        sb.append("\"word_ja\":\"(optional)\",");
        sb.append("\"word_hira_kana\":\"(optional)\",");
        sb.append("\"word_romaji\":\"(optional)\",");
        sb.append("\"word_vi\":\"(optional)\",");
        sb.append("\"example_ja\":\"(optional)\",");
        sb.append("\"example_vi\":\"(optional)\",");
        sb.append("\"topic\":\"(optional)\",");
        sb.append("\"level\":\"(optional)\"");
        sb.append("},");
        sb.append("\"notes\":\"short reason\",");
        sb.append("\"confidence\":0.0");
        sb.append("}]}");
        return sb.toString();
    }
}
