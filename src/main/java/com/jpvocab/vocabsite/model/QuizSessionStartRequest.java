package com.jpvocab.vocabsite.model;

public class QuizSessionStartRequest {
    private Long userId;
    private int sessionIndex;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public int getSessionIndex() {
        return sessionIndex;
    }
    public void setSessionIndex(int sessionIndex) {
        this.sessionIndex = sessionIndex;
    }
}
