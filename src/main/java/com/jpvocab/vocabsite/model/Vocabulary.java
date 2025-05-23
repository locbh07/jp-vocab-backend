package com.jpvocab.vocabsite.model;

public class Vocabulary {
    private int id;
    private String word_ja;
    private String word_hira_kana;
    private String word_romaji;
    private String word_vi;
    private String example_ja;
    private String example_vi;
    private String topic;
    private String level;
    private String image_url;
    private String audio_url;

    // GETTER & SETTER
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getWord_ja() { return word_ja; }
    public void setWord_ja(String word_ja) { this.word_ja = word_ja; }
    
    public String getWord_hira_kana() { return word_hira_kana; }
    public void setWord_hira_kana(String word_hira_kana) { this.word_hira_kana = word_hira_kana; }

    public String getWord_romaji() { return word_romaji; }
    public void setWord_romaji(String word_romaji) { this.word_romaji = word_romaji; }

    public String getWord_vi() { return word_vi; }
    public void setWord_vi(String word_vi) { this.word_vi = word_vi; }

    public String getExample_ja() { return example_ja; }
    public void setExample_ja(String example_ja) { this.example_ja = example_ja; }

    public String getExample_vi() { return example_vi; }
    public void setExample_vi(String example_vi) { this.example_vi = example_vi; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public String getAudio_url() { return audio_url; }
    public void setAudio_url(String audio_url) { this.audio_url = audio_url; }
}
