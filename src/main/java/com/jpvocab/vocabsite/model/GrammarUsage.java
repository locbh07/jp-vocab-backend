package com.jpvocab.vocabsite.model;

public class GrammarUsage {
    private Long  usage_id;
    private String grammar_id;
    private String formation;
    private String example_ja;
    private String example_vi;

    // GETTER & SETTER
    public Long  getUsage_id() { return usage_id; }
    public void setUsage_id(Long  usage_id) { this.usage_id = usage_id; }

    public String getGrammar_id() { return grammar_id; }
    public void setGrammar_id(String grammar_id) { this.grammar_id = grammar_id; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public String getExample_ja() { return example_ja; }
    public void setExample_ja(String example_ja) { this.example_ja = example_ja; }

    public String getExample_vi() { return example_vi; }
    public void setExample_vi(String example_vi) { this.example_vi = example_vi; }
}
