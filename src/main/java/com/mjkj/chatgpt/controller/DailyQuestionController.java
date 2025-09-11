package com.mjkj.chatgpt.controller;

import com.mjkj.chatgpt.model.DailyQuestionModel;
import com.mjkj.chatgpt.model.ResultModel;
import com.mjkj.chatgpt.model.SemanticMatchRequest;
import com.mjkj.chatgpt.model.SemanticMatchResult;
import com.mjkj.chatgpt.model.SimpleSemanticRequest;
import com.mjkj.chatgpt.model.SimpleSemanticResult;
import com.mjkj.chatgpt.service.DailyQuestionService;
import com.mjkj.chatgpt.service.SemanticMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 日常问答控制器
 */
@RestController
@RequestMapping("/api/daily-questions")
@CrossOrigin(origins = "*")
public class DailyQuestionController {
    
    @Autowired
    private DailyQuestionService dailyQuestionService;
    
    @Autowired
    private SemanticMatchService semanticMatchService;
    
    /**
     * 添加日常问答
     */
    @PostMapping("/add")
    public ResultModel addDailyQuestion(@RequestBody DailyQuestionModel dailyQuestion) {
        ResultModel result = new ResultModel();
        
        try {
            boolean success = dailyQuestionService.addDailyQuestion(dailyQuestion);
            if (success) {
                result.setCode(200);
                result.setResultStr("添加成功");
            } else {
                result.setCode(400);
                result.setResultStr("添加失败，请检查数据");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 更新日常问答
     */
    @PutMapping("/update")
    public ResultModel updateDailyQuestion(@RequestBody DailyQuestionModel dailyQuestion) {
        ResultModel result = new ResultModel();
        
        try {
            boolean success = dailyQuestionService.updateDailyQuestion(dailyQuestion);
            if (success) {
                result.setCode(200);
                result.setResultStr("更新成功");
            } else {
                result.setCode(400);
                result.setResultStr("更新失败，请检查数据");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 删除日常问答
     */
    @DeleteMapping("/delete/{id}")
    public ResultModel deleteDailyQuestion(@PathVariable Long id) {
        ResultModel result = new ResultModel();
        
        try {
            boolean success = dailyQuestionService.deleteDailyQuestion(id);
            if (success) {
                result.setCode(200);
                result.setResultStr("删除成功");
            } else {
                result.setCode(400);
                result.setResultStr("删除失败");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据ID获取日常问答
     */
    @GetMapping("/get/{id}")
    public ResultModel getDailyQuestionById(@PathVariable Long id) {
        ResultModel result = new ResultModel();
        
        try {
            DailyQuestionModel dailyQuestion = dailyQuestionService.getDailyQuestionById(id);
            if (dailyQuestion != null) {
                result.setCode(200);
                result.setResultStr("查询成功");
                // 这里可以扩展ResultModel来支持返回数据
            } else {
                result.setCode(404);
                result.setResultStr("未找到该问答");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取所有启用的日常问答
     */
    @GetMapping("/list")
    public ResultModel getAllQuestions() {
        ResultModel result = new ResultModel();
        
        try {
            List<DailyQuestionModel> questions = dailyQuestionService.getAllEnabledQuestions();
            result.setCode(200);
            result.setResultStr("查询成功，共" + questions.size() + "条记录");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 根据分类获取日常问答
     */
    @GetMapping("/category/{category}")
    public ResultModel getQuestionsByCategory(@PathVariable String category) {
        ResultModel result = new ResultModel();
        
        try {
            List<DailyQuestionModel> questions = dailyQuestionService.getQuestionsByCategory(category);
            result.setCode(200);
            result.setResultStr("查询成功，共" + questions.size() + "条记录");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 搜索日常问答
     */
    @GetMapping("/search")
    public ResultModel searchQuestions(@RequestParam String keyword) {
        ResultModel result = new ResultModel();
        
        try {
            List<DailyQuestionModel> questions = dailyQuestionService.searchQuestions(keyword);
            result.setCode(200);
            result.setResultStr("搜索成功，共" + questions.size() + "条记录");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分页获取日常问答
     */
    @GetMapping("/page")
    public ResultModel getQuestionsByPage(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        ResultModel result = new ResultModel();
        
        try {
            List<DailyQuestionModel> questions = dailyQuestionService.getQuestionsByPage(page, size);
            int total = dailyQuestionService.getTotalCount();
            
            Map<String, Object> data = new HashMap<>();
            data.put("questions", questions);
            data.put("total", total);
            data.put("page", page);
            data.put("size", size);
            
            result.setCode(200);
            result.setResultStr("分页查询成功");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取随机日常问答
     */
    @GetMapping("/random")
    public ResultModel getRandomQuestion() {
        ResultModel result = new ResultModel();
        
        try {
            DailyQuestionModel question = dailyQuestionService.getRandomQuestion();
            if (question != null) {
                result.setCode(200);
                result.setResultStr("获取随机问答成功");
            } else {
                result.setCode(404);
                result.setResultStr("暂无可用问答");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResultModel getStatistics() {
        ResultModel result = new ResultModel();
        
        try {
            int total = dailyQuestionService.getTotalCount();
            List<DailyQuestionModel> enabledQuestions = dailyQuestionService.getAllEnabledQuestions();
            
            Map<String, Object> data = new HashMap<>();
            data.put("total", total);
            data.put("enabled", enabledQuestions.size());
            data.put("disabled", total - enabledQuestions.size());
            
            result.setCode(200);
            result.setResultStr("统计信息获取成功");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 语义匹配问答
     */
    @PostMapping("/semantic-match")
    public ResultModel semanticMatch(@RequestBody SemanticMatchRequest request) {
        ResultModel result = new ResultModel();
        
        try {
            SemanticMatchResult matchResult = semanticMatchService.matchQuestion(request);
            
            if (matchResult.getMatched()) {
                result.setCode(200);
                result.setResultStr("语义匹配成功");
                result.setData(matchResult);
            } else {
                result.setCode(404);
                result.setResultStr("未找到匹配的问答");
                result.setData(matchResult);
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 语义匹配多个问答
     */
    @PostMapping("/semantic-match-multiple")
    public ResultModel semanticMatchMultiple(@RequestBody SemanticMatchRequest request) {
        ResultModel result = new ResultModel();
        
        try {
            List<SemanticMatchResult> matchResults = semanticMatchService.matchQuestions(request);
            
            if (!matchResults.isEmpty()) {
                result.setCode(200);
                result.setResultStr("语义匹配成功，找到" + matchResults.size() + "个结果");
                result.setData(matchResults);
            } else {
                result.setCode(404);
                result.setResultStr("未找到匹配的问答");
                result.setData(Collections.emptyList());
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 计算两个问题的相似度
     */
    @PostMapping("/calculate-similarity")
    public ResultModel calculateSimilarity(@RequestParam String question1, @RequestParam String question2) {
        ResultModel result = new ResultModel();
        
        try {
            Double similarity = semanticMatchService.calculateSimilarity(question1, question2);
            
            Map<String, Object> data = new HashMap<>();
            data.put("question1", question1);
            data.put("question2", question2);
            data.put("similarity", similarity);
            
            result.setCode(200);
            result.setResultStr("相似度计算成功");
            result.setData(data);
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 简化语义匹配 - 直接返回答案
     */
    @PostMapping("/simple-match")
    public ResultModel simpleSemanticMatch(@RequestBody SimpleSemanticRequest request) {
        ResultModel result = new ResultModel();
        
        try {
            SimpleSemanticResult matchResult = semanticMatchService.simpleMatch(request);
            
            if (matchResult.getMatched()) {
                result.setCode(200);
                result.setResultStr("语义匹配成功");
                result.setData(matchResult);
            } else {
                result.setCode(404);
                result.setResultStr("未找到匹配的问答");
                result.setData(matchResult);
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
}
