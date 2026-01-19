package com.jpvocab.vocabsite.model;

import java.util.List;

public class LearningTodayResponse {
    private String date;
    private int total;
    private List<LearningWordDto> items;

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public int getTotal() {
        return total;
    }
    public void setTotal(int total) {
        this.total = total;
    }
    public List<LearningWordDto> getItems() {
        return items;
    }
    public void setItems(List<LearningWordDto> items) {
        this.items = items;
    }
}
