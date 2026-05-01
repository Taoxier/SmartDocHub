package com.taoxier.smartdochub.comment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.taoxier.smartdochub.comment.mapper.SensitiveWordMapper;
import com.taoxier.smartdochub.comment.model.entity.SensitiveWord;
import com.taoxier.smartdochub.comment.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 敏感词服务实现 - 使用DFA算法
 */
@Service
@Slf4j
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
        implements SensitiveWordService {

    private final Set<String> seriousCategories = Set.of("POLITICS", "ILLEGAL");

    private volatile Map<String, Integer> sensitiveWordLevelCache = new HashMap<>();
    private volatile Map<Integer, Map<Character, Integer>> dfaStateTable = new HashMap<>();
    private volatile Set<Integer> endStates = new HashSet<>();
    private volatile Map<Integer, String> stateWordMap = new HashMap<>();

    public SensitiveWordServiceImpl() {
        refreshSensitiveWords();
    }

    @Override
    public Map<String, Object> detectSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return Map.of(
                    "hasSensitive", false,
                    "categories", Collections.emptyList(),
                    "words", Collections.emptyList(),
                    "details", Collections.emptyMap());
        }

        Set<String> detectedCategories = new HashSet<>();
        Set<String> allWords = new LinkedHashSet<>();
        Map<String, List<String>> detectedWords = new HashMap<>();

        int currentState = 0;
        int matchStart = -1;
        int matchEnd = -1;
        int bestMatchLength = 0;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            Integer nextState = dfaStateTable.get(currentState).get(ch);

            if (nextState != null) {
                if (matchStart == -1) {
                    matchStart = i;
                }

                if (endStates.contains(nextState)) {
                    matchEnd = i;
                    bestMatchLength = matchEnd - matchStart + 1;
                    currentState = nextState;
                } else {
                    currentState = nextState;
                }
            } else {
                if (matchStart != -1 && bestMatchLength > 0) {
                    String matchedWord = text.substring(matchStart, matchStart + bestMatchLength);
                    String category = getCategoryByWord(matchedWord);

                    if (category != null) {
                        detectedCategories.add(category);
                        allWords.add(matchedWord);
                        detectedWords.computeIfAbsent(category, k -> new ArrayList<>()).add(matchedWord);
                    }

                    int rollbackState = getRollbackState(matchStart, text);
                    if (rollbackState >= 0) {
                        i = matchStart;
                        matchStart = -1;
                        matchEnd = -1;
                        bestMatchLength = 0;
                        currentState = rollbackState;
                        continue;
                    }
                }

                matchStart = -1;
                matchEnd = -1;
                bestMatchLength = 0;
                currentState = 0;
            }
        }

        if (matchStart != -1 && bestMatchLength > 0) {
            String matchedWord = text.substring(matchStart, matchStart + bestMatchLength);
            String category = getCategoryByWord(matchedWord);

            if (category != null) {
                detectedCategories.add(category);
                allWords.add(matchedWord);
                detectedWords.computeIfAbsent(category, k -> new ArrayList<>()).add(matchedWord);
            }
        }

        boolean hasSensitive = !allWords.isEmpty();

        return Map.of(
                "hasSensitive", hasSensitive,
                "categories", new ArrayList<>(detectedCategories),
                "words", new ArrayList<>(allWords),
                "details", detectedWords);
    }

    private int getRollbackState(int startPos, String text) {
        int currentState = 0;
        for (int i = startPos; i < text.length(); i++) {
            char ch = text.charAt(i);
            Integer nextState = dfaStateTable.get(currentState).get(ch);
            if (nextState == null) {
                return currentState;
            }
            currentState = nextState;
        }
        return currentState;
    }

    private String getCategoryByWord(String word) {
        for (Map.Entry<String, Set<String>> entry : sensitiveWordCache.entrySet()) {
            if (entry.getValue().contains(word)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Map<String, Set<String>> sensitiveWordCache = new HashMap<>();

    @Override
    public String getAuditStatus(Map<String, Object> detectionResult) {
        boolean hasSensitive = (Boolean) detectionResult.get("hasSensitive");
        if (!hasSensitive) {
            return "APPROVED";
        }

        List<String> categories = (List<String>) detectionResult.get("categories");
        List<String> words = (List<String>) detectionResult.get("words");

        for (String category : categories) {
            if (seriousCategories.contains(category)) {
                return "REJECTED";
            }
        }

        for (String word : words) {
            Integer level = sensitiveWordLevelCache.get(word);
            if (level != null && level == 2) {
                return "REJECTED";
            }
        }

        return "MANUAL_REVIEW";
    }

    @Override
    public void refreshSensitiveWords() {
        try {
            sensitiveWordCache.clear();
            sensitiveWordLevelCache.clear();
            dfaStateTable.clear();
            endStates.clear();
            stateWordMap.clear();

            List<SensitiveWord> sensitiveWords = list();

            int stateCounter = 1;

            for (SensitiveWord word : sensitiveWords) {
                String category = word.getCategory();
                String w = word.getWord();
                int level = word.getLevel();

                sensitiveWordCache.computeIfAbsent(category, k -> new HashSet<>()).add(w);
                sensitiveWordLevelCache.put(w, level);

                Map<Character, Integer> currentStateMap;
                if (!dfaStateTable.containsKey(0)) {
                    dfaStateTable.put(0, new HashMap<>());
                }
                currentStateMap = dfaStateTable.get(0);

                int currentState = 0;
                for (int i = 0; i < w.length(); i++) {
                    char ch = w.charAt(i);

                    if (currentStateMap.containsKey(ch)) {
                        currentState = currentStateMap.get(ch);
                    } else {
                        currentState = stateCounter++;
                        currentStateMap.put(ch, currentState);
                        dfaStateTable.put(currentState, new HashMap<>());
                    }

                    if (i == w.length() - 1) {
                        endStates.add(currentState);
                        stateWordMap.put(currentState, w);
                    }

                    currentStateMap = dfaStateTable.get(currentState);
                }
            }

            log.info("DFA敏感词缓存刷新成功，加载了 {} 个敏感词，构建了 {} 个状态",
                    sensitiveWords.size(), stateCounter);
        } catch (Exception e) {
            log.error("刷新敏感词缓存失败: {}", e.getMessage());
        }
    }

    @Override
    public List<SensitiveWord> getAllSensitiveWords() {
        return list();
    }

    @Override
    public void addSensitiveWord(String word, String category, int level) {
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWord.setCategory(category);
        sensitiveWord.setLevel(level);
        save(sensitiveWord);
        refreshSensitiveWords();
    }

    @Override
    public void deleteSensitiveWord(Long id) {
        removeById(id);
        refreshSensitiveWords();
    }
}