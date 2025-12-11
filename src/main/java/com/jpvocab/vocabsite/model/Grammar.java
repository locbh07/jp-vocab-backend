package com.jpvocab.vocabsite.model;

public class Grammar {
    private Long  grammar_id;
    private String grammar_point;
    private String grammar_point_romaji;
    private String level;
    private String topic;
    private String meaning_vi;
    private String grammar_usage;
    private String note;

    // GETTER & SETTER
    public Long  getGrammar_id() { return grammar_id; }
    public void setGrammar_id(Long  grammar_id) { this.grammar_id = grammar_id; }

    public String getGrammar_point() { return grammar_point; }
    public void setGrammar_point(String grammar_point) { this.grammar_point = grammar_point; }

    public String getGrammar_point_romaji() { return grammar_point_romaji; }
    public void setGrammar_point_romaji(String grammar_point_romaji) { this.grammar_point_romaji = grammar_point_romaji; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getMeaning_vi() { return meaning_vi; }
    public void setMeaning_vi(String meaning_vi) { this.meaning_vi = meaning_vi; }

    public String getGrammar_usage() { return grammar_usage; }
    public void setGrammar_usage(String grammar_usage) { this.grammar_usage = grammar_usage; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
