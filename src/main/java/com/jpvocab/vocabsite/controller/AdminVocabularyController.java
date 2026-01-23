package com.jpvocab.vocabsite.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import com.jpvocab.vocabsite.service.OpenAiService;
import com.jpvocab.vocabsite.service.VocabularyBulkService;
import com.jpvocab.vocabsite.dto.BulkAiSuggestRequest;
import com.jpvocab.vocabsite.dto.BulkAiSuggestResponse;
import com.jpvocab.vocabsite.dto.BulkApplyRequest;
import com.jpvocab.vocabsite.dto.BulkApplyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminVocabularyController {

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private VocabularyBulkService vocabularyBulkService;

    @Autowired
    private AdminGuard adminGuard;

    public static class AiSuggestRequest {
        public String mode;
        public String language;
        public String notes;
    }

    @GetMapping("/vocabulary")
    public List<Vocabulary> searchVocabulary(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        int offset = safePage * safeSize;
        return vocabularyMapper.searchAdmin(keyword, topic, level, offset, safeSize);
    }

    @GetMapping("/vocabulary/{id}")
    public Vocabulary getVocabulary(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable int id
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        Vocabulary vocab = vocabularyMapper.getById(id);
        if (vocab == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found");
        }
        return vocab;
    }

    @PostMapping("/vocabulary")
    public Vocabulary createVocabulary(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @RequestBody Vocabulary req
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        vocabularyMapper.insertVocabulary(req);
        if (req.getId() > 0) {
            return vocabularyMapper.getById(req.getId());
        }
        return req;
    }

    @PutMapping("/vocabulary/{id}")
    public Vocabulary updateVocabulary(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable int id,
            @RequestBody Vocabulary req
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        Vocabulary existing = vocabularyMapper.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found");
        }
        req.setId(id);
        vocabularyMapper.updateVocabulary(req);
        return vocabularyMapper.getById(id);
    }

    @PostMapping("/vocabulary/{id}/ai-suggest")
    public JsonNode aiSuggest(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable int id,
            @RequestBody AiSuggestRequest req
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        Vocabulary vocab = vocabularyMapper.getById(id);
        if (vocab == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found");
        }
        String mode = req != null && req.mode != null ? req.mode : "fix";
        String language = req != null && req.language != null ? req.language : "vi";
        String notes = req != null ? req.notes : null;

        try {
            return openAiService.suggest(vocab, mode, language, notes);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, ex.getMessage());
        }
    }

    @PutMapping("/vocabulary/{id}/apply")
    public Vocabulary applyVocabulary(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable int id,
            @RequestBody Map<String, Object> body
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        Vocabulary existing = vocabularyMapper.getById(id);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vocabulary not found");
        }
        if (body == null || body.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fields to apply");
        }

        Vocabulary update = new Vocabulary();
        int updatedFields = 0;

        if (body.containsKey("word_ja")) { String v = asString(body.get("word_ja")); if (v != null) { update.setWord_ja(v); updatedFields++; } }
        if (body.containsKey("word_hira_kana")) { String v = asString(body.get("word_hira_kana")); if (v != null) { update.setWord_hira_kana(v); updatedFields++; } }
        if (body.containsKey("word_romaji")) { String v = asString(body.get("word_romaji")); if (v != null) { update.setWord_romaji(v); updatedFields++; } }
        if (body.containsKey("word_vi")) { String v = asString(body.get("word_vi")); if (v != null) { update.setWord_vi(v); updatedFields++; } }
        if (body.containsKey("example_ja")) { String v = asString(body.get("example_ja")); if (v != null) { update.setExample_ja(v); updatedFields++; } }
        if (body.containsKey("example_vi")) { String v = asString(body.get("example_vi")); if (v != null) { update.setExample_vi(v); updatedFields++; } }
        if (body.containsKey("topic")) { String v = asString(body.get("topic")); if (v != null) { update.setTopic(v); updatedFields++; } }
        if (body.containsKey("level")) { String v = asString(body.get("level")); if (v != null) { update.setLevel(v); updatedFields++; } }
        if (body.containsKey("image_url")) { String v = asString(body.get("image_url")); if (v != null) { update.setImage_url(v); updatedFields++; } }
        if (body.containsKey("audio_url")) { String v = asString(body.get("audio_url")); if (v != null) { update.setAudio_url(v); updatedFields++; } }
        if (body.containsKey("core_order")) { Integer v = asInteger(body.get("core_order")); if (v != null) { update.setCore_order(v); updatedFields++; } }

        if (updatedFields == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No supported fields to apply");
        }

        vocabularyMapper.updateVocabularyPartial(id, update);
        return vocabularyMapper.getById(id);
    }

    @PostMapping("/vocabulary/ai-suggest/bulk")
    public BulkAiSuggestResponse bulkAiSuggest(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "50") Integer limit,
            @RequestParam(defaultValue = "10") Integer batchSize,
            @RequestParam(defaultValue = "fix") String mode,
            @RequestBody(required = false) BulkAiSuggestRequest body
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        return vocabularyBulkService.suggestBulk(keyword, topic, level, limit, batchSize, mode, body);
    }

    @PostMapping("/vocabulary/apply/bulk")
    public BulkApplyResponse bulkApply(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @RequestBody BulkApplyRequest body
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        return vocabularyBulkService.applyBulk(body);
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
}
