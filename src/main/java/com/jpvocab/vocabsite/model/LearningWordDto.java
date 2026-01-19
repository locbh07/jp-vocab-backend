package com.jpvocab.vocabsite.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class LearningWordDto {
    private Long wordId;
    private String surface;
    private String reading;
    private String meaning;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date learnedAt;
    private String status;

    public Long getWordId() {
        return wordId;
    }
    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }
    public String getSurface() {
        return surface;
    }
    public void setSurface(String surface) {
        this.surface = surface;
    }
    public String getReading() {
        return reading;
    }
    public void setReading(String reading) {
        this.reading = reading;
    }
    public String getMeaning() {
        return meaning;
    }
    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }
    public Date getLearnedAt() {
        return learnedAt;
    }
    public void setLearnedAt(Date learnedAt) {
        this.learnedAt = learnedAt;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}
