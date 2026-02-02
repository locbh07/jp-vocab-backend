package com.jpvocab.vocabsite.model;

import java.util.Date;

public class UserExamCode {
    private Long id;
    private Long user_id;
    private String level;
    private String code;
    private Boolean enabled;
    private Date updated_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Date getUpdated_at() { return updated_at; }
    public void setUpdated_at(Date updated_at) { this.updated_at = updated_at; }
}
