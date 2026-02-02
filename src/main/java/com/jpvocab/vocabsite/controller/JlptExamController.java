package com.jpvocab.vocabsite.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.jpvocab.vocabsite.mapper.JlptAttemptItemMapper;
import com.jpvocab.vocabsite.mapper.JlptAttemptMapper;
import com.jpvocab.vocabsite.mapper.JlptExamMapper;
import com.jpvocab.vocabsite.mapper.UserMapper;
import com.jpvocab.vocabsite.mapper.UserExamCodeMapper;
import com.jpvocab.vocabsite.model.JlptAttempt;
import com.jpvocab.vocabsite.model.JlptAttemptItem;
import com.jpvocab.vocabsite.model.UserAccount;
import com.jpvocab.vocabsite.service.JlptExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/exam")
public class JlptExamController {

    @Autowired
    private JlptExamMapper examMapper;

    @Autowired
    private JlptAttemptMapper attemptMapper;

    @Autowired
    private JlptAttemptItemMapper itemMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JlptExamService examService;

    @Autowired
    private UserExamCodeMapper examCodeMapper;
    public static class VerifyRequest {
        public Long userId;
        public String code;
    }

    public static class VerifyResponse {
        public boolean allowed;
        public String message;
        public List<String> levels;
        public VerifyResponse(boolean allowed, String message) {
            this.allowed = allowed;
            this.message = message;
        }
    }

    public static class SubmitRequest {
        public Long userId;
        public String level;
        public String examId;
        public Integer durationSec;
        public String code;
        public Map<String, List<String>> answers;
    }

    public static class SubmitResponse {
        public Long attemptId;
        public int scoreTotal;
        public int scoreSec1;
        public int scoreSec2;
        public int scoreSec3;
        public List<JlptAttemptItem> items;
    }

    @PostMapping("/verify-code")
    public VerifyResponse verifyCode(@RequestBody VerifyRequest req) {
        if (req == null || req.userId == null || req.code == null) {
            return new VerifyResponse(false, "Missing userId or code");
        }
        List<String> levels = requireExamAccess(req.userId, req.code, null);
        VerifyResponse res = new VerifyResponse(true, "OK");
        res.levels = levels;
        return res;
    }

    @GetMapping("/list")
    public Map<String, Object> listExams(
            @RequestParam String level,
            @RequestParam Long userId,
            @RequestParam String code
    ) {
        requireExamAccess(userId, code, level);
        List<String> examIds = examMapper.getExamIdsByLevel(level);
        Map<String, Object> res = new HashMap<>();
        res.put("level", level);
        res.put("exams", examIds);
        return res;
    }

    @GetMapping("/{level}/{examId}")
    public Map<String, Object> getExam(
            @PathVariable String level,
            @PathVariable String examId,
            @RequestParam Long userId,
            @RequestParam String code
    ) {
        requireExamAccess(userId, code, level);
        Map<Integer, JsonNode> parts = examService.getExamJsonParts(level, examId);
        if (parts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found");
        }
        Map<String, Object> res = new HashMap<>();
        res.put("level", level);
        res.put("examId", examId);
        res.put("part1", parts.get(1));
        res.put("part2", parts.get(2));
        res.put("part3", parts.get(3));
        return res;
    }

    @PostMapping("/submit")
    public SubmitResponse submit(@RequestBody SubmitRequest req) {
        if (req == null || req.userId == null || req.level == null || req.examId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields");
        }
        requireExamAccess(req.userId, req.code, req.level);

        Map<Integer, List<String>> answersByPart = new HashMap<>();
        if (req.answers != null) {
            answersByPart.put(1, req.answers.getOrDefault("part1", Collections.emptyList()));
            answersByPart.put(2, req.answers.getOrDefault("part2", Collections.emptyList()));
            answersByPart.put(3, req.answers.getOrDefault("part3", Collections.emptyList()));
        }

        JlptExamService.ScoreResult score = examService.scoreAttempt(req.level, req.examId, answersByPart);

        Date finished = new Date();
        Date started = finished;
        if (req.durationSec != null && req.durationSec > 0) {
            started = new Date(finished.getTime() - (long) req.durationSec * 1000L);
        }

        JlptAttempt attempt = new JlptAttempt();
        attempt.setUser_id(req.userId);
        attempt.setLevel(req.level);
        attempt.setExam_id(req.examId);
        attempt.setStarted_at(started);
        attempt.setFinished_at(finished);
        attempt.setDuration_sec(req.durationSec);
        attempt.setScore_total(score.total());
        attempt.setScore_sec1(score.score1);
        attempt.setScore_sec2(score.score2);
        attempt.setScore_sec3(score.score3);

        attemptMapper.insertAttempt(attempt);

        for (JlptAttemptItem item : score.items) {
            item.setAttempt_id(attempt.getId());
        }
        if (!score.items.isEmpty()) {
            itemMapper.insertItems(score.items);
        }

        SubmitResponse res = new SubmitResponse();
        res.attemptId = attempt.getId();
        res.scoreTotal = score.total();
        res.scoreSec1 = score.score1;
        res.scoreSec2 = score.score2;
        res.scoreSec3 = score.score3;
        res.items = score.items;
        return res;
    }

    @GetMapping("/history")
    public Map<String, Object> history(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam String code
    ) {
        requireExamAccess(userId, code, null);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<JlptAttempt> attempts = attemptMapper.getAttemptsByUser(userId, safeSize, offset);
        Map<String, Object> res = new HashMap<>();
        res.put("items", attempts);
        res.put("page", safePage);
        res.put("size", safeSize);
        return res;
    }

    @GetMapping("/history/{attemptId}")
    public Map<String, Object> historyDetail(
            @PathVariable Long attemptId,
            @RequestParam Long userId,
            @RequestParam String code
    ) {
        requireExamAccess(userId, code, null);
        JlptAttempt attempt = attemptMapper.getAttemptById(attemptId);
        if (attempt == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Attempt not found");
        }
        if (!attempt.getUser_id().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        List<JlptAttemptItem> items = itemMapper.getItemsByAttempt(attemptId);
        Map<String, Object> res = new HashMap<>();
        res.put("attempt", attempt);
        res.put("items", items);
        return res;
    }

    private List<String> requireExamAccess(Long userId, String code, String level) {
        UserAccount user = userMapper.findById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return Arrays.asList("N5", "N4", "N3", "N2", "N1");
        }
        if (user.getExamEnabled() == null || !user.getExamEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam access not enabled");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid code");
        }
        List<String> levels = examCodeMapper.getAllowedLevels(userId, code);
        if (levels == null || levels.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Exam code not set");
        }
        if (level != null && !levels.contains(level)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid code");
        }
        return levels;
    }
}
