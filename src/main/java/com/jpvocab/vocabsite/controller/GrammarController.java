package com.jpvocab.vocabsite.controller;

import com.jpvocab.vocabsite.mapper.GrammarMapper;
import com.jpvocab.vocabsite.mapper.GrammarUsageMapper;
import com.jpvocab.vocabsite.model.Grammar;
import com.jpvocab.vocabsite.model.GrammarUsage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grammar")
public class GrammarController {

    @Autowired
    private GrammarMapper grammarMapper;

    @Autowired
    private GrammarUsageMapper usageMapper;

    // API: GET /grammar?level=N5
    @GetMapping
    public List<Grammar> getAllGrammar(@RequestParam(defaultValue = "N5") String level) {
        return grammarMapper.getAllGrammar(level);
    }

    // API: GET /grammar/{id}
    @GetMapping("/{id}")
    public Map<String, Object> getGrammarDetail(@PathVariable("id") Long  id) {
        Grammar grammar = grammarMapper.getGrammarById(id);
        List<GrammarUsage> usages = usageMapper.getUsagesByGrammarId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("grammar", grammar);
        response.put("usages", usages);

        return response;
    }
}
