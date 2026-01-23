package com.jpvocab.vocabsite.dto;

import java.util.List;

public class BulkAiSuggestRequest {
    private List<String> fields;
    private Boolean strictJson;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Boolean getStrictJson() {
        return strictJson;
    }

    public void setStrictJson(Boolean strictJson) {
        this.strictJson = strictJson;
    }
}
