package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.VocabularyMapper;
import com.jpvocab.vocabsite.model.Vocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    @PatchMapping("/{id}")
public Vocabulary updateVocabulary(
        @PathVariable int id,
        @RequestBody Vocabulary req
) {
    Vocabulary old = vocabularyMapper.getById(id);
    if (old == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Vocabulary not found: " + id
        );
    }

    // merge: field nào null/blank thì giữ nguyên
    Vocabulary merged = mergeVocabulary(old, req);
    merged.setId(id);

    vocabularyMapper.updateVocabulary(merged);
    return vocabularyMapper.getById(id);
}

private Vocabulary mergeVocabulary(Vocabulary old, Vocabulary req) {

    old.setWord_ja(pick(req.getWord_ja(), old.getWord_ja()));
    old.setWord_hira_kana(pick(req.getWord_hira_kana(), old.getWord_hira_kana()));
    old.setWord_romaji(pick(req.getWord_romaji(), old.getWord_romaji()));
    old.setWord_vi(pick(req.getWord_vi(), old.getWord_vi()));
    old.setExample_ja(pick(req.getExample_ja(), old.getExample_ja()));
    old.setExample_vi(pick(req.getExample_vi(), old.getExample_vi()));
    old.setTopic(pick(req.getTopic(), old.getTopic()));
    old.setLevel(pick(req.getLevel(), old.getLevel()));
    old.setImage_url(pick(req.getImage_url(), old.getImage_url()));
    old.setAudio_url(pick(req.getAudio_url(), old.getAudio_url()));

    if (req.getCore_order() != null) {
        old.setCore_order(req.getCore_order());
    }

    return old;
}

private String pick(String incoming, String current) {
    if (incoming == null) return current;
    String trimmed = incoming.trim();
    return trimmed.isEmpty() ? current : incoming;
}
}
