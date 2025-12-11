package com.jpvocab.vocabsite.model;

import java.util.Date;

public class UserLearningPlan {

    private Long id;
    private Long user_id;

    private Integer total_words;
    private Integer target_months;

    private Date start_date;
    private Date target_date;

    private Integer daily_new_words;
    private Integer is_active;

    private Date created_at;
    private Date updated_at;

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }

    public Integer getTotal_words() { return total_words; }
    public void setTotal_words(Integer total_words) { this.total_words = total_words; }

    public Integer getTarget_months() { return target_months; }
    public void setTarget_months(Integer target_months) { this.target_months = target_months; }

    public Date getStart_date() { return start_date; }
    public void setStart_date(Date start_date) { this.start_date = start_date; }

    public Date getTarget_date() { return target_date; }
    public void setTarget_date(Date target_date) { this.target_date = target_date; }

    public Integer getDaily_new_words() { return daily_new_words; }
    public void setDaily_new_words(Integer daily_new_words) { this.daily_new_words = daily_new_words; }

    public Integer getIs_active() { return is_active; }
    public void setIs_active(Integer is_active) { this.is_active = is_active; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
}
