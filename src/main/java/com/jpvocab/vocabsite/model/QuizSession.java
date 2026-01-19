package com.jpvocab.vocabsite.model;

import java.util.Date;

public class QuizSession {
    private Long id;
    private Long user_id;
    private java.sql.Date session_date;
    private Integer session_index;
    private Integer batch_size;
    private Date created_at;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getUser_id() {
        return user_id;
    }
    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }
    public java.sql.Date getSession_date() {
        return session_date;
    }
    public void setSession_date(java.sql.Date session_date) {
        this.session_date = session_date;
    }
    public Integer getSession_index() {
        return session_index;
    }
    public void setSession_index(Integer session_index) {
        this.session_index = session_index;
    }
    public Integer getBatch_size() {
        return batch_size;
    }
    public void setBatch_size(Integer batch_size) {
        this.batch_size = batch_size;
    }
    public Date getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
