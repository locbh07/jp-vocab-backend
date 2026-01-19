package com.jpvocab.vocabsite.model;

import java.util.List;

public class QuizSessionStartResponse {
    private Long sessionId;
    private int batchSize;
    private List<QuizWordDto> items;

    public Long getSessionId() {
        return sessionId;
    }
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
    public int getBatchSize() {
        return batchSize;
    }
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    public List<QuizWordDto> getItems() {
        return items;
    }
    public void setItems(List<QuizWordDto> items) {
        this.items = items;
    }
}
