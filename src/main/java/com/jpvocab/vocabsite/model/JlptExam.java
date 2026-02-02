package com.jpvocab.vocabsite.model;

public class JlptExam {
    private Long id;
    private String level;
    private String exam_id;
    private Integer part;
    private String source_file;
    private String json_data;
    private String created_at;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getExam_id() { return exam_id; }
    public void setExam_id(String exam_id) { this.exam_id = exam_id; }

    public Integer getPart() { return part; }
    public void setPart(Integer part) { this.part = part; }

    public String getSource_file() { return source_file; }
    public void setSource_file(String source_file) { this.source_file = source_file; }

    public String getJson_data() { return json_data; }
    public void setJson_data(String json_data) { this.json_data = json_data; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}
