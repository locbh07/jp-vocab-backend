package com.jpvocab.vocabsite.model;

public class QuizWordDto {
    private Long wordId;
    private String surface;
    private String reading;
    private String meaning;

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
}
