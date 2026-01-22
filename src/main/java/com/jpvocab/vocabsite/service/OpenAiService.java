package com.jpvocab.vocabsite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        Map response = restTemplate.postForObject(API_URL, entity, Map.class);
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

        try {
            return mapper.readTree((String) contentObj);
        } catch (Exception ex) {
            throw new IllegalStateException("OpenAI returned non-JSON content");
        }
    }

    private Map<String, Object> message(String role, String content) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
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
        sb.append("Do not include fields you are not changing, except notes.");
        return sb.toString();
    }
}
