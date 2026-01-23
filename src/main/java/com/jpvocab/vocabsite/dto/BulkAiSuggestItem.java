package com.jpvocab.vocabsite.dto;

import java.util.Map;

public class BulkAiSuggestItem {
    private Long id;
    private VocabularySnapshot original;
    private Map<String, Object> suggested;
    private String notes;
    private Double confidence;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VocabularySnapshot getOriginal() {
        return original;
    }

    public void setOriginal(VocabularySnapshot original) {
        this.original = original;
    }

    public Map<String, Object> getSuggested() {
        return suggested;
    }

    public void setSuggested(Map<String, Object> suggested) {
        this.suggested = suggested;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
