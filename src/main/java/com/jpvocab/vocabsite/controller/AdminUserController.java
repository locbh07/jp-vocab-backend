package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.UserMapper;
import com.jpvocab.vocabsite.mapper.UserVocabProgressMapper;
import com.jpvocab.vocabsite.mapper.UserExamCodeMapper;
import com.jpvocab.vocabsite.model.DailyStudyRow;
import com.jpvocab.vocabsite.model.UserAccount;
import com.jpvocab.vocabsite.model.UserExamCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserVocabProgressMapper progressMapper;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    private UserExamCodeMapper examCodeMapper;

    public static class AdminUserUpdateRequest {
        public String fullName;
        public String email;
        public String role;
        public Boolean examEnabled;
        public String examCode;
    }

    public static class AdminUserStatsResponse {
        public int totalStudyDays;
        public String lastStudyDate;
        public int totalLearnedVocab;
        public int currentStreakDays;
    }

    public static class ExamCodeUpdateItem {
        public String level;
        public String code;
        public Boolean enabled;
    }

    @GetMapping("/users")
    public List<UserAccount> searchUsers(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        int offset = safePage * safeSize;
        List<UserAccount> users = userMapper.searchUsers(keyword, offset, safeSize);
        hidePasswords(users);
        return users;
    }

    @GetMapping("/users/{idOrUsername}")
    public UserAccount getUser(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable String idOrUsername
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        UserAccount user = findUser(idOrUsername);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setPasswordHash(null);
        return user;
    }

    @PutMapping("/users/{idOrUsername}")
    public UserAccount updateUser(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable String idOrUsername,
            @RequestBody AdminUserUpdateRequest req
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        UserAccount user = findUser(idOrUsername);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        if (req == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fields to update");
        }

        UserAccount update = new UserAccount();
        update.setFullName(req.fullName);
        update.setEmail(req.email);
        update.setRole(req.role);
        update.setExamEnabled(req.examEnabled);
        update.setExamCode(req.examCode);
        if (update.getFullName() == null && update.getEmail() == null && update.getRole() == null && update.getExamEnabled() == null && update.getExamCode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No fields to update");
        }
        userMapper.updateUserFields(user.getId(), update);

        UserAccount refreshed = userMapper.findById(user.getId());
        if (refreshed != null) {
            refreshed.setPasswordHash(null);
        }
        return refreshed;
    }

    @GetMapping("/users/{userId}/stats")
    public AdminUserStatsResponse getUserStats(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable Long userId
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);

        AdminUserStatsResponse res = new AdminUserStatsResponse();
        res.totalStudyDays = progressMapper.countStudyDays(userId);
        res.totalLearnedVocab = progressMapper.countLearnedByUser(userId);

        java.sql.Date last = progressMapper.getLastStudyDate(userId);
        res.lastStudyDate = last == null ? null : last.toString();

        List<DailyStudyRow> rows = progressMapper.getStudyDays(userId, 365);
        res.currentStreakDays = computeStreak(rows);
        return res;
    }

    @GetMapping("/users/{userId}/exam-codes")
    public List<UserExamCode> getExamCodes(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable Long userId
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        return examCodeMapper.getByUser(userId);
    }

    @PutMapping("/users/{userId}/exam-codes")
    public Map<String, Object> updateExamCodes(
            @RequestHeader(value = "X-Admin-Username", required = false) String adminUsername,
            @RequestHeader(value = "X-Admin-UserId", required = false) Long adminUserId,
            @PathVariable Long userId,
            @RequestBody List<ExamCodeUpdateItem> items
    ) {
        adminGuard.requireAdmin(adminUsername, adminUserId);
        if (items == null || items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No exam codes provided");
        }
        List<UserExamCode> upserts = new ArrayList<>();
        for (ExamCodeUpdateItem item : items) {
            if (item == null || item.level == null || item.code == null) continue;
            UserExamCode row = new UserExamCode();
            row.setUser_id(userId);
            row.setLevel(item.level);
            row.setCode(item.code);
            row.setEnabled(item.enabled != null ? item.enabled : Boolean.TRUE);
            upserts.add(row);
        }
        if (upserts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid exam codes provided");
        }
        int updated = examCodeMapper.upsertCodes(upserts);
        Map<String, Object> res = new HashMap<>();
        res.put("updated", updated);
        return res;
    }

    private UserAccount findUser(String idOrUsername) {
        if (idOrUsername != null && idOrUsername.matches("\\d+")) {
            return userMapper.findById(Long.parseLong(idOrUsername));
        }
        return userMapper.findByUsername(idOrUsername);
    }

    private void hidePasswords(List<UserAccount> users) {
        if (users == null) return;
        for (UserAccount user : users) {
            if (user != null) {
                user.setPasswordHash(null);
            }
        }
    }

    private int computeStreak(List<DailyStudyRow> rows) {
        if (rows == null || rows.isEmpty()) return 0;
        Set<String> dates = new HashSet<>();
        for (DailyStudyRow row : rows) {
            if (row.getStudy_date() != null) {
                dates.add(row.getStudy_date().toString());
            }
        }
        if (dates.isEmpty()) return 0;

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        cal.setTime(today);
        while (true) {
            String key = new java.sql.Date(cal.getTimeInMillis()).toString();
            if (dates.contains(key)) {
                streak++;
                cal.add(Calendar.DATE, -1);
            } else {
                break;
            }
        }
        return streak;
    }
}
