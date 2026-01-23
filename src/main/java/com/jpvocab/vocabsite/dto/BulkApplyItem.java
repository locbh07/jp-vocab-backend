package com.jpvocab.vocabsite.dto;

import java.util.Map;

public class BulkApplyItem {
    private Long id;
    private Map<String, Object> patch;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getPatch() {
        return patch;
    }

    public void setPatch(Map<String, Object> patch) {
        this.patch = patch;
    }
}
