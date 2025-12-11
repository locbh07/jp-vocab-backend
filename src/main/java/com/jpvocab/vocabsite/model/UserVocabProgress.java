package com.jpvocab.vocabsite.model;

import java.util.Date;

public class UserVocabProgress {

    private Long id;
    private Long user_id;
    private Long vocab_id;
    private Long plan_id;

    private Integer stage;
    private Date next_review_date;
    private Date last_reviewed_at;
    private Integer times_reviewed;
    private Integer last_result;
    private Integer is_mastered;

    private Date created_at;
    private Date updated_at;
    
    private Date first_seen_date;

    public Date getFirst_seen_date() {
		return first_seen_date;
	}
	public void setFirst_seen_date(Date first_seen_date) {
		this.first_seen_date = first_seen_date;
	}
	public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }

    public Long getVocab_id() { return vocab_id; }
    public void setVocab_id(Long vocab_id) { this.vocab_id = vocab_id; }

    public Long getPlan_id() { return plan_id; }
    public void setPlan_id(Long plan_id) { this.plan_id = plan_id; }

    public Integer getStage() { return stage; }
    public void setStage(Integer stage) { this.stage = stage; }

    public Date getNext_review_date() { return next_review_date; }
    public void setNext_review_date(Date next_review_date) { this.next_review_date = next_review_date; }

    public Date getLast_reviewed_at() { return last_reviewed_at; }
    public void setLast_reviewed_at(Date last_reviewed_at) { this.last_reviewed_at = last_reviewed_at; }

    public Integer getTimes_reviewed() { return times_reviewed; }
    public void setTimes_reviewed(Integer times_reviewed) { this.times_reviewed = times_reviewed; }

    public Integer getLast_result() { return last_result; }
    public void setLast_result(Integer last_result) { this.last_result = last_result; }

    public Integer getIs_mastered() { return is_mastered; }
    public void setIs_mastered(Integer is_mastered) { this.is_mastered = is_mastered; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
}
