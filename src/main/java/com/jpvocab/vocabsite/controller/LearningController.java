package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.UserLearningPlanMapper;
import com.jpvocab.vocabsite.mapper.UserVocabProgressMapper;
import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.DailyStudyRow;
import com.jpvocab.vocabsite.model.LearningDashboardResponse;
import com.jpvocab.vocabsite.model.UserLearningPlan;
import com.jpvocab.vocabsite.model.UserVocabProgress;
import com.jpvocab.vocabsite.model.Vocabulary;
import com.jpvocab.vocabsite.model.UserReviewLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/learning")
public class LearningController {

	@Autowired
	private UserLearningPlanMapper planMapper;

	@Autowired
	private UserVocabProgressMapper progressMapper;

	@Autowired
	private VocabularyMapper vocabularyMapper;

	// có thì Autowire, không có thì bỏ dòng này đi
	@Autowired(required = false)
	private com.jpvocab.vocabsite.mapper.UserReviewLogMapper reviewLogMapper;

	// ---------- A) Tạo / cập nhật lộ trình ----------
	@PostMapping("/plan")
	public UserLearningPlan createPlan(@RequestParam("userId") Long userId,
			@RequestParam("targetMonths") Integer targetMonths) {

		int totalWords = vocabularyMapper.countCoreWords(); // 6727

		// tắt plan cũ
		planMapper.deactivateAllPlans(userId);

		Date start = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.MONTH, targetMonths);
		Date target = cal.getTime();

		int daily = (int) Math.ceil(totalWords / (targetMonths * 30.0));

		UserLearningPlan plan = new UserLearningPlan();
		plan.setUser_id(userId);
		plan.setTotal_words(totalWords);
		plan.setTarget_months(targetMonths);
		plan.setStart_date(start);
		plan.setTarget_date(target);
		plan.setDaily_new_words(daily);
		plan.setIs_active(1);

		planMapper.createPlan(plan);

		return planMapper.getActivePlan(userId);
	}

	// ---------- lấy plan đang active ----------
	@GetMapping("/activePlan")
	public UserLearningPlan getActivePlan(@RequestParam("userId") Long userId) {
		return planMapper.getActivePlan(userId);
	}

	// ---------- B) Từ mới hôm nay ----------
	@GetMapping("/new-words")
	public List<Vocabulary> getNewWords(@RequestParam("userId") Long userId) {
		UserLearningPlan plan = planMapper.getActivePlan(userId);
		if (plan == null || plan.getDaily_new_words() == null || plan.getDaily_new_words() <= 0) {
			return Collections.emptyList();
		}

		int dailyLimit = plan.getDaily_new_words();

		// Lấy "hôm nay" kiểu java.sql.Date để so sánh với cột DATE trong DB
		java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

		// Đếm xem hôm nay đã có bao nhiêu từ mới được học lần đầu
		int usedToday = progressMapper.countNewWordsToday(userId, today);

		int remaining = dailyLimit - usedToday;
		if (remaining <= 0) {
			// Đã đủ quota từ mới hôm nay -> không cho thêm nữa
			return Collections.emptyList();
		}

		// Chỉ lấy thêm đúng số "remaining" từ mới chưa có progress
		return vocabularyMapper.getNewWordsForUser(userId, remaining);
	}

	// ---------- C) Từ cần ôn hôm nay ----------
	@GetMapping("/reviews")
	public List<Vocabulary> getDueReviewWords(@RequestParam("userId") Long userId) {
		List<UserVocabProgress> progresses = progressMapper.getDueReviews(userId);
		if (progresses == null || progresses.isEmpty()) {
			return Collections.emptyList();
		}

		List<Vocabulary> result = new ArrayList<>();
		for (UserVocabProgress p : progresses) {
			if (p.getVocab_id() == null)
				continue;
			Vocabulary v = vocabularyMapper.getById(p.getVocab_id().intValue());
			if (v != null)
				result.add(v);
		}
		return result;
	}

	// ---------- D) Nhận kết quả quiz / SRS ----------
	public static class ReviewRequest {
		public Long userId;
		public Long vocabId;
		public Boolean correct;
		public String mode;
	}

	@PostMapping("/review-result")
	public String submitReviewResult(@RequestBody ReviewRequest req) {

		Long userId = req.userId;
		Long vocabId = req.vocabId;
		boolean correct = Boolean.TRUE.equals(req.correct);

		UserVocabProgress progress = progressMapper.findProgress(userId, vocabId);
		UserLearningPlan plan = planMapper.getActivePlan(userId);

		if (progress == null) {
			progress = new UserVocabProgress();
			progress.setUser_id(userId);
			progress.setVocab_id(vocabId);
			progress.setPlan_id(plan != null ? plan.getId() : null);
			progress.setStage(0);
			progress.setTimes_reviewed(0);
			progress.setIs_mastered(0);
			progress.setFirst_seen_date(new java.sql.Date(System.currentTimeMillis()));
		}

		int stage = progress.getStage() == null ? 0 : progress.getStage();
		int[] intervals = { 0, 1, 3, 7, 30, 90 };

		if (correct) {
			stage = Math.min(stage + 1, 5);
			progress.setLast_result(1);
		} else {
			stage = Math.max(stage - 1, 0);
			progress.setLast_result(0);
		}

		progress.setStage(stage);
		if (stage >= 5) {
			progress.setIs_mastered(1);
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DAY_OF_MONTH, intervals[stage]);
		progress.setNext_review_date(cal.getTime());
		progress.setLast_reviewed_at(new Date());
		progress.setTimes_reviewed((progress.getTimes_reviewed() == null ? 0 : progress.getTimes_reviewed() + 1));

		if (progress.getId() == null) {
			progressMapper.insertProgress(progress);
		} else {
			progressMapper.updateProgress(progress);
		}

		if (reviewLogMapper != null) {
			UserReviewLog log = new UserReviewLog();
			log.setUserId(userId);
			log.setVocabId(vocabId);
			log.setReviewTime(new Date());
			log.setResult(correct ? 1 : 0);
			log.setMode(req.mode != null ? req.mode : "review");
			reviewLogMapper.insertLog(log);
		}

		return "OK";
	}

	// ---------- E) Dashboard ----------
	@GetMapping("/dashboard")
	public LearningDashboardResponse getDashboard(@RequestParam("userId") Long userId) {
		LearningDashboardResponse res = new LearningDashboardResponse();

		int total = vocabularyMapper.countCoreWords();
		int learned = progressMapper.countLearnedByUser(userId);
		int mastered = progressMapper.countMasteredByUser(userId);

		res.setTotalCoreWords(total);
		res.setLearnedWords(learned);
		res.setMasteredWords(mastered);
		res.setInProgressWords(Math.max(learned - mastered, 0));
//		double percent = total == 0 ? 0.0 : (mastered * 100.0 / total);
        double percent = total == 0 ? 0.0 : (learned * 100.0 / total);
		res.setProgressPercent(percent);

		java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

		// 1) Lấy thống kê 60 ngày gần nhất
		List<DailyStudyRow> rows = reviewLogMapper.getDailyStudyStats(userId, 60);

		Map<String, Integer> recentStudyDays = new LinkedHashMap<>();
		int todayNewWords = 0;
		int todayReviews = 0; // nếu bạn muốn tách rõ new/review, cần log mode

		for (DailyStudyRow row : rows) {
			String dateStr = row.getStudy_date().toString(); // "2025-12-10"
			recentStudyDays.put(dateStr, row.getWords());

			if (row.getStudy_date().equals(today)) {
				// Hiện tại rows.words = số từ có log trong ngày
				// Nếu muốn tách new/review, cần thêm logic dựa vào mode/result
				todayReviews = row.getWords();
			}
		}

		// 2) Tính streak: đếm số ngày liên tiếp tính từ hôm nay ngược lại
		int currentStreak = 0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);

		while (true) {
			String key = new java.sql.Date(cal.getTimeInMillis()).toString();
			if (recentStudyDays.containsKey(key) && recentStudyDays.get(key) > 0) {
				currentStreak++;
				// lùi 1 ngày
				cal.add(Calendar.DATE, -1);
			} else {
				break;
			}
		}

		// gán vào response
		todayNewWords = reviewLogMapper.countTodayNewWords(userId, today);
		res.setTodayNewWords(todayNewWords);
		res.setTodayReviews(todayReviews);
		res.setCurrentStreak(currentStreak);
		res.setRecentStudyDays(recentStudyDays);

		return res;
	}
}
