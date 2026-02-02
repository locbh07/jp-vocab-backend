package com.jpvocab.vocabsite.model;

import java.util.Date;

public class JlptAttempt {
    private Long id;
    private Long user_id;
    private String level;
    private String exam_id;
    private Date started_at;
    private Date finished_at;
    private Integer duration_sec;
    private Integer score_total;
    private Integer score_sec1;
    private Integer score_sec2;
    private Integer score_sec3;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUser_id() { return user_id; }
    public void setUser_id(Long user_id) { this.user_id = user_id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getExam_id() { return exam_id; }
    public void setExam_id(String exam_id) { this.exam_id = exam_id; }

    public Date getStarted_at() { return started_at; }
    public void setStarted_at(Date started_at) { this.started_at = started_at; }

    public Date getFinished_at() { return finished_at; }
    public void setFinished_at(Date finished_at) { this.finished_at = finished_at; }

    public Integer getDuration_sec() { return duration_sec; }
    public void setDuration_sec(Integer duration_sec) { this.duration_sec = duration_sec; }

    public Integer getScore_total() { return score_total; }
    public void setScore_total(Integer score_total) { this.score_total = score_total; }

    public Integer getScore_sec1() { return score_sec1; }
    public void setScore_sec1(Integer score_sec1) { this.score_sec1 = score_sec1; }

    public Integer getScore_sec2() { return score_sec2; }
    public void setScore_sec2(Integer score_sec2) { this.score_sec2 = score_sec2; }

    public Integer getScore_sec3() { return score_sec3; }
    public void setScore_sec3(Integer score_sec3) { this.score_sec3 = score_sec3; }
}
