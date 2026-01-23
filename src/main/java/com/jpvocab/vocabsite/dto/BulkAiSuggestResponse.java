package com.jpvocab.vocabsite.dto;

import java.util.List;

public class BulkAiSuggestResponse {
    private String mode;
    private int limit;
    private int batchSize;
    private List<BulkAiSuggestItem> items;
    private List<BulkAiSuggestError> errors;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public List<BulkAiSuggestItem> getItems() {
        return items;
    }

    public void setItems(List<BulkAiSuggestItem> items) {
        this.items = items;
    }

    public List<BulkAiSuggestError> getErrors() {
        return errors;
    }

    public void setErrors(List<BulkAiSuggestError> errors) {
        this.errors = errors;
    }
}
