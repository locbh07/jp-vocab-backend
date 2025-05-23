package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
@CrossOrigin(origins = "*")
public class VocabularyController {

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @GetMapping("/all")
    public List<Vocabulary> getAllVocabulary() {
        return vocabularyMapper.findAll();
    }

    @GetMapping("/topics")
    public List<String> getAllTopics() {
        return vocabularyMapper.findDistinctTopics();
    }

    @GetMapping("/by-topic")
    public List<Vocabulary> getVocabularyByTopic(@RequestParam String topic) {
        return vocabularyMapper.findByTopic(topic);
    }
}