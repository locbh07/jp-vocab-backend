package com.jpvocab.vocabsite.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpvocab.vocabsite.mapper.JlptExamMapper;
import com.jpvocab.vocabsite.model.JlptAttemptItem;
import com.jpvocab.vocabsite.model.JlptExam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JlptExamService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JlptExamMapper examMapper;

    public Map<Integer, JsonNode> getExamJsonParts(String level, String examId) {
        List<JlptExam> parts = examMapper.getExamParts(level, examId);
        Map<Integer, JsonNode> result = new HashMap<>();
        for (JlptExam part : parts) {
            if (part.getJson_data() == null) continue;
            try {
                JsonNode json = mapper.readTree(part.getJson_data());
                result.put(part.getPart(), json);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public ScoreResult scoreAttempt(String level,
                                    String examId,
                                    Map<Integer, List<String>> answersByPart) {
        Map<Integer, JsonNode> parts = getExamJsonParts(level, examId);
        ScoreResult result = new ScoreResult();
        for (int part = 1; part <= 3; part++) {
            JsonNode partJson = parts.get(part);
            List<String> answers = answersByPart.get(part);
            if (partJson == null) continue;
            scorePart(part, partJson, answers, result);
        }
        return result;
    }

    private void scorePart(int part, JsonNode partJson, List<String> answers, ScoreResult result) {
        if (answers == null) answers = new ArrayList<>();
        JsonNode sections = partJson.get("sections");
        if (sections == null || !sections.isArray()) return;

        int flatIndex = 0;
        int sectionIndex = 0;
        for (JsonNode section : sections) {
            JsonNode questions = section.get("questions");
            if (questions == null || !questions.isArray()) {
                sectionIndex++;
                continue;
            }
            int questionIndex = 0;
            for (JsonNode q : questions) {
                String correct = textValue(q, "answer");
                String selected = flatIndex < answers.size() ? answers.get(flatIndex) : null;
                boolean isCorrect = selected != null && correct != null && correct.equals(selected);

                JlptAttemptItem item = new JlptAttemptItem();
                item.setPart(part);
                item.setSection_index(sectionIndex);
                item.setQuestion_index(questionIndex);
                item.setQuestion_id(textValue(q, "qid"));
                item.setSelected(selected);
                item.setCorrect_answer(correct);
                item.setIs_correct(isCorrect);
                try {
                    item.setQuestion_json(mapper.writeValueAsString(q));
                } catch (Exception ignored) {
                }
                result.items.add(item);

                if (isCorrect) {
                    result.increment(part);
                }
                flatIndex++;
                questionIndex++;
            }
            sectionIndex++;
        }
    }

    private String textValue(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }

    public static class ScoreResult {
        public int score1 = 0;
        public int score2 = 0;
        public int score3 = 0;
        public List<JlptAttemptItem> items = new ArrayList<>();

        public int total() {
            return score1 + score2 + score3;
        }

        public void increment(int part) {
            if (part == 1) score1++;
            else if (part == 2) score2++;
            else if (part == 3) score3++;
        }
    }
}
