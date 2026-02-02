package com.jpvocab.vocabsite.model;

public class JlptAttemptItem {
    private Long id;
    private Long attempt_id;
    private Integer part;
    private Integer section_index;
    private Integer question_index;
    private String question_id;
    private String selected;
    private String correct_answer;
    private Boolean is_correct;
    private String question_json;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAttempt_id() { return attempt_id; }
    public void setAttempt_id(Long attempt_id) { this.attempt_id = attempt_id; }

    public Integer getPart() { return part; }
    public void setPart(Integer part) { this.part = part; }

    public Integer getSection_index() { return section_index; }
    public void setSection_index(Integer section_index) { this.section_index = section_index; }

    public Integer getQuestion_index() { return question_index; }
    public void setQuestion_index(Integer question_index) { this.question_index = question_index; }

    public String getQuestion_id() { return question_id; }
    public void setQuestion_id(String question_id) { this.question_id = question_id; }

    public String getSelected() { return selected; }
    public void setSelected(String selected) { this.selected = selected; }

    public String getCorrect_answer() { return correct_answer; }
    public void setCorrect_answer(String correct_answer) { this.correct_answer = correct_answer; }

    public Boolean getIs_correct() { return is_correct; }
    public void setIs_correct(Boolean is_correct) { this.is_correct = is_correct; }

    public String getQuestion_json() { return question_json; }
    public void setQuestion_json(String question_json) { this.question_json = question_json; }
}
