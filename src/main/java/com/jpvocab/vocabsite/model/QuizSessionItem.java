package com.jpvocab.vocabsite.model;

import java.util.Date;

public class QuizSessionItem {
    private Long id;
    private Long session_id;
    private Long vocab_id;
    private Integer item_order;
    private Date created_at;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getSession_id() {
        return session_id;
    }
    public void setSession_id(Long session_id) {
        this.session_id = session_id;
    }
    public Long getVocab_id() {
        return vocab_id;
    }
    public void setVocab_id(Long vocab_id) {
        this.vocab_id = vocab_id;
    }
    public Integer getItem_order() {
        return item_order;
    }
    public void setItem_order(Integer item_order) {
        this.item_order = item_order;
    }
    public Date getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }
}
