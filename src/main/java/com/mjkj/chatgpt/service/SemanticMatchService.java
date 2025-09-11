package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.model.SemanticMatchRequest;
import com.mjkj.chatgpt.model.SemanticMatchResult;
import com.mjkj.chatgpt.model.SimpleSemanticRequest;
import com.mjkj.chatgpt.model.SimpleSemanticResult;
import java.util.List;

/**
 * 语义匹配服务接口
 */
public interface SemanticMatchService {
    
    /**
     * 语义匹配单个问题
     */
    SemanticMatchResult matchQuestion(SemanticMatchRequest request);
    
    /**
     * 语义匹配多个问题（返回多个结果）
     */
    List<SemanticMatchResult> matchQuestions(SemanticMatchRequest request);
    
    /**
     * 计算两个问题的相似度
     */
    Double calculateSimilarity(String question1, String question2);
    
    /**
     * 简化语义匹配 - 直接返回答案
     */
    SimpleSemanticResult simpleMatch(SimpleSemanticRequest request);
}
