package com.jpvocab.vocabsite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpvocab.vocabsite.dto.BulkAiSuggestError;
import com.jpvocab.vocabsite.dto.BulkAiSuggestItem;
import com.jpvocab.vocabsite.dto.BulkAiSuggestRequest;
import com.jpvocab.vocabsite.dto.BulkAiSuggestResponse;
import com.jpvocab.vocabsite.dto.BulkApplyFailure;
import com.jpvocab.vocabsite.dto.BulkApplyItem;
import com.jpvocab.vocabsite.dto.BulkApplyRequest;
import com.jpvocab.vocabsite.dto.BulkApplyResponse;
import com.jpvocab.vocabsite.dto.VocabularySnapshot;
import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class VocabularyBulkService {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;
    private static final int DEFAULT_BATCH_SIZE = 10;
    private static final int MAX_BATCH_SIZE = 20;
    private static final List<String> DEFAULT_FIELDS = Arrays.asList(
            "word_vi",
            "example_ja",
            "example_vi",
            "word_hira_kana",
            "word_romaji",
            "topic",
            "level"
    );

    private static final Set<String> ALLOWED_PATCH_FIELDS = new HashSet<>(Arrays.asList(
            "word_ja",
            "word_hira_kana",
            "word_romaji",
            "word_vi",
            "example_ja",
            "example_vi",
            "topic",
            "level",
            "image_url",
            "audio_url",
            "core_order"
    ));

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @Autowired
    private OpenAiService openAiService;

    public BulkAiSuggestResponse suggestBulk(String keyword,
                                             String topic,
                                             String level,
                                             Integer limit,
                                             Integer batchSize,
                                             String mode,
                                             BulkAiSuggestRequest request) {
        int safeLimit = normalizeLimit(limit);
        int safeBatch = normalizeBatchSize(batchSize);
        String safeMode = (mode == null || mode.trim().isEmpty()) ? "fix" : mode.trim();

        List<String> fields = request != null && request.getFields() != null && !request.getFields().isEmpty()
                ? request.getFields()
                : DEFAULT_FIELDS;
        boolean strictJson = request == null || request.getStrictJson() == null || Boolean.TRUE.equals(request.getStrictJson());

        List<Vocabulary> vocabList = vocabularyMapper.searchAdminBulk(keyword, topic, level, safeLimit);
        if (vocabList == null) {
            vocabList = Collections.emptyList();
        }

        List<BulkAiSuggestItem> items = new ArrayList<>();
        List<BulkAiSuggestError> errors = new ArrayList<>();

        int batchIndex = 0;
        for (int i = 0; i < vocabList.size(); i += safeBatch) {
            int end = Math.min(i + safeBatch, vocabList.size());
            List<Vocabulary> chunk = vocabList.subList(i, end);
            List<Long> chunkIds = extractIds(chunk);

            try {
                JsonNode result = openAiService.suggestBulk(chunk, safeMode, fields, strictJson);
                Map<Long, SuggestionPayload> payloads = parseSuggestions(result, chunkIds);
                List<Long> missingIds = new ArrayList<>();

                for (Vocabulary vocab : chunk) {
                    BulkAiSuggestItem item = new BulkAiSuggestItem();
                    item.setId((long) vocab.getId());
                    item.setOriginal(buildSnapshot(vocab));

                    SuggestionPayload payload = payloads.get((long) vocab.getId());
                    if (payload != null) {
                        item.setSuggested(payload.suggested);
                        item.setNotes(payload.notes);
                        item.setConfidence(payload.confidence);
                    } else {
                        missingIds.add((long) vocab.getId());
                        item.setSuggested(Collections.<String, Object>emptyMap());
                        item.setNotes("Missing suggestion for item");
                        item.setConfidence(0.0);
                    }
                    items.add(item);
                }

                if (!missingIds.isEmpty()) {
                    BulkAiSuggestError error = new BulkAiSuggestError();
                    error.setBatchIndex(batchIndex);
                    error.setMessage("Missing suggestions for some items");
                    error.setIds(missingIds);
                    errors.add(error);
                }
            } catch (Exception ex) {
                BulkAiSuggestError error = new BulkAiSuggestError();
                error.setBatchIndex(batchIndex);
                error.setMessage(ex.getMessage());
                error.setIds(chunkIds);
                errors.add(error);
            }

            batchIndex++;
        }

        BulkAiSuggestResponse response = new BulkAiSuggestResponse();
        response.setMode(safeMode);
        response.setLimit(safeLimit);
        response.setBatchSize(safeBatch);
        response.setItems(items);
        response.setErrors(errors);
        return response;
    }

    @Transactional
    public BulkApplyResponse applyBulk(BulkApplyRequest request) {
        BulkApplyResponse response = new BulkApplyResponse();
        List<BulkApplyFailure> failures = new ArrayList<>();
        int updated = 0;

        if (request == null || request.getUpdates() == null || request.getUpdates().isEmpty()) {
            response.setSuccess(false);
            response.setUpdated(0);
            response.setFailed(Collections.singletonList(failure(null, "No updates provided")));
            return response;
        }

        for (BulkApplyItem update : request.getUpdates()) {
            if (update == null || update.getId() == null) {
                failures.add(failure(null, "Missing id"));
                continue;
            }
            Map<String, Object> patch = update.getPatch();
            if (patch == null || patch.isEmpty()) {
                failures.add(failure(update.getId(), "Empty patch"));
                continue;
            }
            if (patch.containsKey("id")) {
                failures.add(failure(update.getId(), "Field 'id' is not allowed"));
                continue;
            }

            String invalidField = findInvalidField(patch.keySet());
            if (invalidField != null) {
                failures.add(failure(update.getId(), "Field not allowed: " + invalidField));
                continue;
            }

            Vocabulary vocabPatch = buildPatch(patch);
            if (vocabPatch == null) {
                failures.add(failure(update.getId(), "No valid fields to update"));
                continue;
            }

            try {
                int count = vocabularyMapper.updateVocabularyPartial(update.getId().intValue(), vocabPatch);
                if (count > 0) {
                    updated++;
                } else {
                    failures.add(failure(update.getId(), "Vocabulary not found"));
                }
            } catch (Exception ex) {
                failures.add(failure(update.getId(), ex.getMessage()));
            }
        }

        response.setSuccess(failures.isEmpty());
        response.setUpdated(updated);
        response.setFailed(failures);
        return response;
    }

    private int normalizeLimit(Integer limit) {
        int value = limit == null ? DEFAULT_LIMIT : limit;
        if (value < 1) {
            value = DEFAULT_LIMIT;
        }
        return Math.min(value, MAX_LIMIT);
    }

    private int normalizeBatchSize(Integer batchSize) {
        int value = batchSize == null ? DEFAULT_BATCH_SIZE : batchSize;
        if (value < 1) {
            value = DEFAULT_BATCH_SIZE;
        }
        return Math.min(value, MAX_BATCH_SIZE);
    }

    private List<Long> extractIds(List<Vocabulary> chunk) {
        List<Long> ids = new ArrayList<>();
        for (Vocabulary vocab : chunk) {
            ids.add((long) vocab.getId());
        }
        return ids;
    }

    private VocabularySnapshot buildSnapshot(Vocabulary vocab) {
        VocabularySnapshot snapshot = new VocabularySnapshot();
        snapshot.setWord_ja(vocab.getWord_ja());
        snapshot.setWord_vi(vocab.getWord_vi());
        snapshot.setExample_ja(vocab.getExample_ja());
        snapshot.setExample_vi(vocab.getExample_vi());
        snapshot.setTopic(vocab.getTopic());
        snapshot.setLevel(vocab.getLevel());
        snapshot.setWord_hira_kana(vocab.getWord_hira_kana());
        snapshot.setWord_romaji(vocab.getWord_romaji());
        return snapshot;
    }

    private Map<Long, SuggestionPayload> parseSuggestions(JsonNode root, List<Long> validIds) {
        Map<Long, SuggestionPayload> result = new HashMap<>();
        if (root == null || !root.has("items") || !root.get("items").isArray()) {
            return result;
        }

        // Parse the model JSON payload into a map keyed by id.
        Set<Long> valid = new HashSet<>(validIds);
        for (JsonNode node : root.get("items")) {
            JsonNode idNode = node.get("id");
            if (idNode == null || !idNode.isNumber()) {
                continue;
            }
            long id = idNode.asLong();
            if (!valid.contains(id)) {
                continue;
            }
            Map<String, Object> suggested = new LinkedHashMap<>();
            JsonNode suggestedNode = node.get("suggested");
            if (suggestedNode != null && suggestedNode.isObject()) {
                suggested = openAiService.convertJsonToMap(suggestedNode);
            }

            String notes = null;
            JsonNode notesNode = node.get("notes");
            if (notesNode != null && notesNode.isTextual()) {
                notes = notesNode.asText();
            }

            Double confidence = null;
            JsonNode confidenceNode = node.get("confidence");
            if (confidenceNode != null && confidenceNode.isNumber()) {
                confidence = confidenceNode.asDouble();
            }

            SuggestionPayload payload = new SuggestionPayload();
            payload.suggested = suggested;
            payload.notes = notes;
            payload.confidence = confidence;
            result.put(id, payload);
        }
        return result;
    }

    private String findInvalidField(Set<String> keys) {
        for (String key : keys) {
            if (!ALLOWED_PATCH_FIELDS.contains(key)) {
                return key;
            }
        }
        return null;
    }

    private Vocabulary buildPatch(Map<String, Object> patch) {
        Vocabulary update = new Vocabulary();
        int updatedFields = 0;

        if (patch.containsKey("word_ja")) { String v = asString(patch.get("word_ja")); if (v != null) { update.setWord_ja(v); updatedFields++; } }
        if (patch.containsKey("word_hira_kana")) { String v = asString(patch.get("word_hira_kana")); if (v != null) { update.setWord_hira_kana(v); updatedFields++; } }
        if (patch.containsKey("word_romaji")) { String v = asString(patch.get("word_romaji")); if (v != null) { update.setWord_romaji(v); updatedFields++; } }
        if (patch.containsKey("word_vi")) { String v = asString(patch.get("word_vi")); if (v != null) { update.setWord_vi(v); updatedFields++; } }
        if (patch.containsKey("example_ja")) { String v = asString(patch.get("example_ja")); if (v != null) { update.setExample_ja(v); updatedFields++; } }
        if (patch.containsKey("example_vi")) { String v = asString(patch.get("example_vi")); if (v != null) { update.setExample_vi(v); updatedFields++; } }
        if (patch.containsKey("topic")) { String v = asString(patch.get("topic")); if (v != null) { update.setTopic(v); updatedFields++; } }
        if (patch.containsKey("level")) { String v = asString(patch.get("level")); if (v != null) { update.setLevel(v); updatedFields++; } }
        if (patch.containsKey("image_url")) { String v = asString(patch.get("image_url")); if (v != null) { update.setImage_url(v); updatedFields++; } }
        if (patch.containsKey("audio_url")) { String v = asString(patch.get("audio_url")); if (v != null) { update.setAudio_url(v); updatedFields++; } }
        if (patch.containsKey("core_order")) { Integer v = asInteger(patch.get("core_order")); if (v != null) { update.setCore_order(v); updatedFields++; } }

        return updatedFields == 0 ? null : update;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BulkApplyFailure failure(Long id, String message) {
        BulkApplyFailure failure = new BulkApplyFailure();
        failure.setId(id);
        failure.setMessage(message);
        return failure;
    }

    private static class SuggestionPayload {
        private Map<String, Object> suggested;
        private String notes;
        private Double confidence;
    }
}
