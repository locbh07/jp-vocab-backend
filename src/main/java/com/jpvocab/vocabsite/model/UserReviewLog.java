package com.jpvocab.vocabsite.model;

import java.util.Date;

public class UserReviewLog {
    private Long id;
    private Long userId;
    private Long vocabId;
    private Date reviewTime;
    private Integer result;
    private String mode;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Long getVocabId() {
		return vocabId;
	}
	public void setVocabId(Long vocabId) {
		this.vocabId = vocabId;
	}
	public Date getReviewTime() {
		return reviewTime;
	}
	public void setReviewTime(Date reviewTime) {
		this.reviewTime = reviewTime;
	}
	public Integer getResult() {
		return result;
	}
	public void setResult(Integer result) {
		this.result = result;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}

    
}