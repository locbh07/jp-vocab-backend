// com.jpvocab.vocabsite.model.LearningDashboardResponse

package com.jpvocab.vocabsite.model;

import java.util.Map;

public class LearningDashboardResponse {
    private int totalCoreWords;
    private int learnedWords;
    private int masteredWords;
    private int inProgressWords;
    private double progressPercent;
    
    private int todayNewWords;
    private int todayReviews;
    private int currentStreak;
    private Map<String, Integer> recentStudyDays; // "2025-12-10" -> 24 từ


    public int getTodayNewWords() {
		return todayNewWords;
	}
	public void setTodayNewWords(int todayNewWords) {
		this.todayNewWords = todayNewWords;
	}
	public int getTodayReviews() {
		return todayReviews;
	}
	public void setTodayReviews(int todayReviews) {
		this.todayReviews = todayReviews;
	}
	public int getCurrentStreak() {
		return currentStreak;
	}
	public void setCurrentStreak(int currentStreak) {
		this.currentStreak = currentStreak;
	}
	public Map<String, Integer> getRecentStudyDays() {
		return recentStudyDays;
	}
	public void setRecentStudyDays(Map<String, Integer> recentStudyDays) {
		this.recentStudyDays = recentStudyDays;
	}
	public int getTotalCoreWords() { return totalCoreWords; }
    public void setTotalCoreWords(int totalCoreWords) { this.totalCoreWords = totalCoreWords; }

    public int getLearnedWords() { return learnedWords; }
    public void setLearnedWords(int learnedWords) { this.learnedWords = learnedWords; }

    public int getMasteredWords() { return masteredWords; }
    public void setMasteredWords(int masteredWords) { this.masteredWords = masteredWords; }

    public int getInProgressWords() { return inProgressWords; }
    public void setInProgressWords(int inProgressWords) { this.inProgressWords = inProgressWords; }

    public double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
}
