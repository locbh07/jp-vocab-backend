package com.jpvocab.vocabsite.model;

import java.util.Date;

public class JlptExamRevision {
    private Long id;
    private String level;
    private String exam_id;
    private Integer part;
    private Long editor_id;
    private String note;
    private String json_data;
    private Date created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getExam_id() { return exam_id; }
    public void setExam_id(String exam_id) { this.exam_id = exam_id; }

    public Integer getPart() { return part; }
    public void setPart(Integer part) { this.part = part; }

    public Long getEditor_id() { return editor_id; }
    public void setEditor_id(Long editor_id) { this.editor_id = editor_id; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getJson_data() { return json_data; }
    public void setJson_data(String json_data) { this.json_data = json_data; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
}
