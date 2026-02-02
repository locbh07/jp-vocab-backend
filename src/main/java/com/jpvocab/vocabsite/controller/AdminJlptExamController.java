package com.jpvocab.vocabsite.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpvocab.vocabsite.mapper.JlptExamMapper;
import com.jpvocab.vocabsite.mapper.JlptExamRevisionMapper;
import com.jpvocab.vocabsite.model.JlptExam;
import com.jpvocab.vocabsite.model.JlptExamRevision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/exam")
public class AdminJlptExamController {

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private JlptExamMapper examMapper;

    @Autowired
    private JlptExamRevisionMapper revisionMapper;

    private final ObjectMapper mapper = new ObjectMapper();

    public static class UpdateRequest {
        public Object json;
        public String note;
    }

    @GetMapping("/{level}/{examId}/part/{part}")
    public Map<String, Object> getPart(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable String level,
            @PathVariable String examId,
            @PathVariable int part
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        JlptExam exam = examMapper.getExamPart(level, examId, part);
        if (exam == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam part not found");
        }
        List<JlptExamRevision> revisions = revisionMapper.getRevisions(level, examId, part);
        Map<String, Object> res = new HashMap<>();
        res.put("level", level);
        res.put("examId", examId);
        res.put("part", part);
        res.put("json", exam.getJson_data());
        res.put("revisions", revisions);
        return res;
    }

    @PutMapping("/{level}/{examId}/part/{part}")
    public Map<String, Object> updatePart(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable String level,
            @PathVariable String examId,
            @PathVariable int part,
            @RequestBody UpdateRequest req
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        if (req == null || req.json == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing json");
        }
        JlptExam exam = examMapper.getExamPart(level, examId, part);
        if (exam == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam part not found");
        }

        // Save current snapshot into revision history
        JlptExamRevision revision = new JlptExamRevision();
        revision.setLevel(level);
        revision.setExam_id(examId);
        revision.setPart(part);
        revision.setEditor_id(adminUserId);
        revision.setNote(req.note);
        revision.setJson_data(exam.getJson_data());
        revisionMapper.insertRevision(revision);

        String jsonString = toJsonString(req.json);
        examMapper.updateExamPart(level, examId, part, jsonString);

        Map<String, Object> res = new HashMap<>();
        res.put("updated", true);
        return res;
    }

    @PostMapping("/{level}/{examId}/part/{part}/restore/{revisionId}")
    public Map<String, Object> restorePart(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable String level,
            @PathVariable String examId,
            @PathVariable int part,
            @PathVariable Long revisionId
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        JlptExam exam = examMapper.getExamPart(level, examId, part);
        if (exam == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam part not found");
        }
        JlptExamRevision revision = revisionMapper.getById(revisionId);
        if (revision == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Revision not found");
        }

        // Save current snapshot before restore
        JlptExamRevision snapshot = new JlptExamRevision();
        snapshot.setLevel(level);
        snapshot.setExam_id(examId);
        snapshot.setPart(part);
        snapshot.setEditor_id(adminUserId);
        snapshot.setNote("restore");
        snapshot.setJson_data(exam.getJson_data());
        revisionMapper.insertRevision(snapshot);

        examMapper.updateExamPart(level, examId, part, revision.getJson_data());
        Map<String, Object> res = new HashMap<>();
        res.put("restored", true);
        return res;
    }

    private String toJsonString(Object payload) {
        if (payload instanceof String) {
            return (String) payload;
        }
        try {
            JsonNode node = mapper.valueToTree(payload);
            return mapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON payload");
        }
    }
}
