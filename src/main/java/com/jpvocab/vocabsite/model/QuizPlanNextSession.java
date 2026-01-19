package com.jpvocab.vocabsite.model;

import java.util.List;

public class QuizPlanNextSession {
    private int sessionIndex;
    private List<QuizWordDto> items;

    public int getSessionIndex() {
        return sessionIndex;
    }
    public void setSessionIndex(int sessionIndex) {
        this.sessionIndex = sessionIndex;
    }
    public List<QuizWordDto> getItems() {
        return items;
    }
    public void setItems(List<QuizWordDto> items) {
        this.items = items;
    }
}
