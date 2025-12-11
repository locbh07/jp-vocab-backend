package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vocabulary")
public class VocabularyController {

    @Autowired
    private VocabularyMapper vocabularyMapper;

    @GetMapping("/all")
    public List<Vocabulary> getAll(@RequestParam(defaultValue = "3000_common_") String prefix) {
        return vocabularyMapper.getWordsByPrefix(prefix);
    }

    @GetMapping("/topics")
    public List<String> getTopics(@RequestParam(defaultValue = "3000_common_") String prefix) {
        return vocabularyMapper.getTopicsByPrefix(prefix);
    }
}
