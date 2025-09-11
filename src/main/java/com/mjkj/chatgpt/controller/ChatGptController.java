/**
 * Copyright (c) 2018-2028, Chill Zhuang 庄骞 (smallchill@163.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mjkj.chatgpt.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mjkj.chatgpt.model.*;
import com.mjkj.chatgpt.service.BankQuestionService;
import com.mjkj.chatgpt.service.IChatGPTService;
import com.mjkj.chatgpt.service.SemanticMatchService;
import com.mjkj.chatgpt.service.WenDaService;
import com.mjkj.chatgpt.strategy.KeywordStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true")
@RequestMapping("chatgpt")
public class ChatGptController {

    @Autowired
    private IChatGPTService chatGPTService;
    @Autowired
    private BankQuestionService bankQuestionService;
    @Autowired
    private SemanticMatchService semanticMatchService;
    @Autowired
    private WenDaService wenDaService;
    @Value("${config.aivt.url:http://127.0.0.1:8082/send}")
    private String aivtUrl;
    @Value("${config.ai.wenda.url:http://127.0.0.1:17860/api/chat}")
    private String aiWendaUrl;
    @Value("${config.ai.video.url:http://127.0.0.1:8091/show/local}")
    private String aiVideoUrl;
    @Value("http://192.168.1.27:9001/v1/chat-messages")
    private String aiDifyChatMessagesUrl;
    @Value("${config.ai.dify.key:Bearer app-P9erUWPiZx6HZ8zOj9hW5LzE}")
    private String aiDifyKey;
    @Value("${config.ai.prompt.value:你是智能百科,每个问题尽量不超过20字,回答内容不要带格式}")
    private String aiPromptValue;
    //设置最大问题数量
    @Value("${config.ai.question.max:5}")
    private int maxQuestionWaitingCount;

    @Autowired
    RedisTemplate redisTemplate;
    private final String currentCountKey = "api:currentCount"; // Redis中存储当前计数的键
    private final String questionSetKey = "ceyan:questions";
    private final String questionWaitingKey = "ceyan:questionWaiting:python"; // Redis中存储问题等待队列的键
    private final String limitCountKey = "api:limitCount"; // Redis中存储限制调用次数的键
//    private static String aesKey = "vWkzDxDfXruFpgjDH7Jy0mIWamCQvdct";
    private static String aesKey = "EgzdVGYalHE1pUNMO3CeIKatKmuocz07";



    //获取gpt内容
    @PostMapping({"/getContent"})
    public ResultModel getContent(@RequestBody String paramStr) {
        try {
            if (StrUtil.isEmpty(paramStr)) {
                return this.getErrorModel("参数为空2");
            }
            AES aes = SecureUtil.aes(aesKey.getBytes());
            // 解密为字符串
            String jsonStr = aes.decryptStr(paramStr, CharsetUtil.CHARSET_UTF_8);

            ChatGptParam gptModel = JSONObject.parseObject(jsonStr, ChatGptParam.class);
            if(ObjectUtil.isEmpty(gptModel)){
                return this.getErrorModel("参数为空3");
            }

            String chatContent = chatGPTService.getChatContent(gptModel);
            return this.getSuccessModel(chatContent);
        } catch (Exception e) {
           return this.getErrorModel(e.getMessage());
        }
    }

    /**
     * gpt-3.5-turbo-0301
     * @param paramStr
     * @return
     */
    @PostMapping({"/api/getContent"})
    public ResultModel getChatGptContent(@RequestBody String paramStr) {
        try {
            if (StrUtil.isEmpty(paramStr)) {
                return this.getErrorModel("参数为空2");
            }
            AES aes = SecureUtil.aes(aesKey.getBytes());
            // 解密为字符串
            String jsonStr = aes.decryptStr(paramStr, CharsetUtil.CHARSET_UTF_8);

            ChatGptPublicParam param = JSONObject.parseObject(jsonStr, ChatGptPublicParam.class);
            if(ObjectUtil.isEmpty(param)){
                return this.getErrorModel("参数为空3");
            }
            String chatContent = chatGPTService.getChatGptPublic(param);
            return this.getSuccessModel(chatContent);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    /**
     * 文心一言大模型
     * @param str
     * @return
     */
    @PostMapping({"/api/getWenxinContent"})
    public ResultModel getWenxinContent(@RequestBody String str) {
        try {
            log.info("getWenxinContent str:{}",str);
            if (StrUtil.isEmpty(str)) {
                return this.getErrorModel("参数为空2");
            }
            AES aes = SecureUtil.aes(aesKey.getBytes());
            // 解密为字符串
            String jsonStr = aes.decryptStr(str, CharsetUtil.CHARSET_UTF_8);

            ChatGptPublicParam param = JSONObject.parseObject(jsonStr, ChatGptPublicParam.class);
            if(ObjectUtil.isEmpty(param)){
                return this.getErrorModel("参数为空3");
            }
            String chatContent = chatGPTService.getWenxinContent(param);
            return this.getSuccessModel(chatContent);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }


    //闻达大模型
    @PostMapping({"/api/getWendaContent"})
    public ResultModel getWendaContent(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            //判断wenDaParam中的prompt 是否为 小蓝小蓝 如果是 执行小蓝小蓝后面的切割,否则不操作
            String prompt = wenDaParam.getPrompt();
            WenDaBody wenDaBody = null;
            if (StrUtil.isNotEmpty(prompt) && (prompt.contains("小蓝，小蓝")||prompt.contains("小蓝小蓝")  || prompt.contains("小兰，小兰") || prompt.contains("小兰小兰"))) {
                //判断是否包含小蓝小蓝 或者 小兰小兰 或者 小蓝,小蓝 或者 小兰,小兰
                //如果包含,则切割
                String splits = null;
                if (prompt.contains("小蓝小蓝")) {
                    //切割从第5位开始截取
                    splits = prompt.split("小蓝小蓝")[1];
                } else if (prompt.contains("小兰小兰")) {
                    //切割从第5位开始截取
                    splits = prompt.split("小兰小兰")[1];
                } else if (prompt.contains("小蓝，小蓝")) {
                    //切割从第5位开始截取
                    splits = prompt.split("小蓝，小蓝")[1];
                } else if (prompt.contains("小兰，小兰")) {
                    //切割从第5位开始截取
                    splits = prompt.split("小兰，小兰")[1];
                }
                if (splits.length() <= 3) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"我在,请问有什么需要帮助的\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                String split = splits;
                wenDaParam.setPrompt(split);
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"小蓝还需要继续学习，您可以拨打24小时水务客服热线，我们有工作人员为您解答\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }else {
                    //调用成功传参
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+wenDaBody.getContent()+"\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }

            }

//            if (ObjectUtil.isEmpty(wenDaBody)) {
//                String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"小蓝还需要继续学习，您可以拨打24小时水务客服热线，我们有工作人员为您解答\"}";
//                //调用失败传参
//                HttpRequest request  = HttpRequest.post(aivtUrl)
//                        .header("Content-Type", "application/json");
//                request.body(jsonStr)
//                        .execute().body();
//            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(wenDaBody);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    @PostMapping({"/api/getWendaContent/v2"})
    public ResultModel getWendaContentV2(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            String[] prefixes = {
                    "小园", "小原", "晓园", "小员", "小圆", "小袁",
                    "小猿", "小缘", "小辕", "小媛", "小元", "小源",
                    "晓媛", "晓园"
            };
            // Build regex pattern (without ^)
            StringBuilder patternBuilder = new StringBuilder("(");  // Removed ^
            for (int i = 0; i < prefixes.length; i++) {
                if (i > 0) {
                    patternBuilder.append("|");
                }
                patternBuilder.append(Pattern.quote(prefixes[i]));
            }
            patternBuilder.append(")");
            Pattern pattern = Pattern.compile(patternBuilder.toString());

            String testStr = wenDaParam.getPrompt();
            Matcher matcher = pattern.matcher(testStr);
            if (matcher.find()) {
                String matchedPrefix = matcher.group(1);
                //剩余部分
                String remaining = testStr.substring(matchedPrefix.length());
                //判断剩余部分长度是否大于3 如果是 则继续执行,否则直接返回
                if (! ObjectUtil.isEmpty(remaining)) {
                    wenDaParam.setPrompt(remaining);
                }else {
                    return this.getErrorModel("没有匹配到");
                }
            } else {
                return this.getErrorModel("没有匹配到");
            }
            String prompt = wenDaParam.getPrompt();
            WenDaBody wenDaBody = null;
            if (StrUtil.isNotEmpty(prompt) ) {
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (prompt.contains("唤醒小元")) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"你好，我是雄安兴元的数字人小元，,,,有什么我可以帮助的吗？请在问题前加上小元来唤醒我。\"}";
                    //调用传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                //如果包含,则切割
                if (prompt.length() <= 3) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"我在,请问有什么需要帮助的\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    String jsonStr = "{\"type\":\"tuning\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+prompt+"\"}";
                    log.info("getWendaContentV2 str:{}",jsonStr);
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }else {
                    //调用成功传参
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+wenDaBody.getContent()+"\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(wenDaBody);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    @PostMapping({"/api/getWendaContent/v3"})
    public ResultModel getWendaContentV3(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            String testStr = wenDaParam.getPrompt();
             //判断是否包含 你好小元 你好小园 你好小原 你好小员 你好小圆 你好小袁 你好小猿 你好小缘 你好小辕 你好小媛 你好小元 你好小源
            //如果包含,则切割
            String[] prefixes = {
                    "你好小园", "你好小原", "你好小员", "你好小圆", "你好小袁",
                    "你好小猿", "你好小缘", "你好小辕", "你好小媛", "你好小元", "你好小源",
                    "你好，小袁", "你好，小猿", "你好，小圆", "你好，小园",
                    "你好，小原", "你好，小元", "你好，小源", "你好，小辕",
            };
            // Build regex pattern (without ^)
            StringBuilder patternBuilder = new StringBuilder("(");  // Removed ^
            for (int i = 0; i < prefixes.length; i++) {
                if (i > 0) {
                    patternBuilder.append("|");
                }
                patternBuilder.append(Pattern.quote(prefixes[i]));
            }
            patternBuilder.append(")");
            Pattern pattern = Pattern.compile(patternBuilder.toString());
            Matcher matcher = pattern.matcher(testStr);
            boolean matches = matcher.find();
            log.info("getWendaContent str:{} , matcher:{}",testStr,matches);
            if (matches) {
                log.info("pick success , matcher:{}",testStr,matcher.find());
                String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"我在\"}";
                //调用失败传参
                HttpRequest request = HttpRequest.post(aivtUrl)
                        .header("Content-Type", "application/json");
                request.body(jsonStr)
                        .execute().body();
                log.info("pick success , matcher:{} return success",testStr,matches);
                return this.getSuccessModel("成功推送");
            }
            log.info("getWendaContent str:{}",testStr);
            //判断testStr长度是否大于3 如果是 则继续执行,否则直接返回
            if (testStr.length() <= 5) {
                    return this.getErrorModel("回答长度太短");
            }
            String prompt = wenDaParam.getPrompt();
            log.info("getWendaContentV3 str:{}",prompt);
            WenDaBody wenDaBody = null;
            if (StrUtil.isNotEmpty(prompt) ) {
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (prompt.contains("唤醒小元")) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"你好，我是雄安兴元的数字人小元,请说小元来唤醒我。\"}";
                    //调用传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    String jsonStr = "{\"type\":\"tuning\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+prompt+"\"}";
                    log.info("getWendaContentV3 str:{}",jsonStr);
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }else {
                    //调用成功传参
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+wenDaBody.getContent()+"\"}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(wenDaBody);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    @PostMapping({"/api/getWendaContent/v4"})
    public ResultModel getWendaContentV4(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContentV4 str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            String testStr = wenDaParam.getPrompt();
            //判断是否包含 你好小元 你好小园 你好小原 你好小员 你好小圆 你好小袁 你好小猿 你好小缘 你好小辕 你好小媛 你好小元 你好小源
            //如果包含,则切割
            String[] prefixes = {
                    "你好小园", "你好小原", "你好小员", "你好小圆", "你好小袁",
                    "你好小猿", "你好小缘", "你好小辕", "你好小媛", "你好小元", "你好小源",
                    "你好，小袁", "你好，小猿", "你好，小圆", "你好，小园",
                    "你好，小原", "你好，小元", "你好，小源", "你好，小辕",
            };
            // Build regex pattern (without ^)
            StringBuilder patternBuilder = new StringBuilder("(");  // Removed ^
            for (int i = 0; i < prefixes.length; i++) {
                if (i > 0) {
                    patternBuilder.append("|");
                }
                patternBuilder.append(Pattern.quote(prefixes[i]));
            }
            patternBuilder.append(")");
            Pattern pattern = Pattern.compile(patternBuilder.toString());
            Matcher matcher = pattern.matcher(testStr);
            boolean matches = matcher.find();
            log.info("getWendaContent str:{} , matcher:{}",testStr,matches);
            if (matches) {
                log.info("pick success , matcher:{}",testStr,matcher.find());
                String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"我在\"}";
                //调用失败传参
                HttpRequest request = HttpRequest.post(aivtUrl)
                        .header("Content-Type", "application/json");
                request.body(jsonStr)
                        .execute().body();
                log.info("pick success , matcher:{} return success",testStr,matches);
                return this.getSuccessModel("成功推送");
            }
            log.info("getWendaContent str:{}",testStr);
            //判断testStr长度是否大于3 如果是 则继续执行,否则直接返回
            if (testStr.length() <= 3) {
                return this.getErrorModel("回答长度太短");
            }
            String prompt = wenDaParam.getPrompt();
            log.info("getWendaContentV4 str:{}",prompt);
            WenDaBody wenDaBody = null;
            if (StrUtil.isNotEmpty(prompt) ) {
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (prompt.contains("唤醒小元")) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"你好，我是雄安兴元的数字人小元,请说小元来唤醒我。\"}";
                    //调用传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                log.info("getWendaContentV4 wenDaBody:{}",wenDaBody);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    //调用失败则再次调用本地大模型接口: 127.0.0.1:17860/chat  {"prompt":"测试传输","keyword":"测试传输","temperature":0.8,"top_p":0.8,"max_length":4096,"history":[]}
                    String sendStr = "{\"prompt\":\"你是智能百科,每个问题尽量不超过20字,回答内容不要带格式,问题如下:"+prompt+"\",\"keyword\":\"你是智能百科,每个问题尽量不超过20字,回答内容不要带格式,问题如下:"+prompt+"\",\"temperature\":0.8,\"top_p\":0.8,\"max_length\":4096,\"history\":[]}";
                    log.info("getWendaContentV4 str:{}",sendStr);
                    HttpRequest requestWenda = HttpRequest.post(aiWendaUrl)
                            .header("Content-Type", "application/json");
                    String result = requestWenda.body(sendStr)
                            .execute().body();
                    // 将result中的 \n 替换成句号
                    //猫砂 \n 猫粮 \n
                    result = result.replaceAll("\\\\n", "。");
                    //猫砂、猫粮、猫砂盆、猫抓板、猫草、疫苗、猫窝等
                    //将result中 、替换成逗号
                    result = result.replaceAll("、", ",");
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+result+"\"}";
                    log.info("getWendaContentV4 str:{}",jsonStr);
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aivtUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }else {
                    //调用成功传参
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\""+wenDaBody.getContent()+"\"}";
                    //调用失败传参
//                    HttpRequest request  = HttpRequest.post(aivtUrl)
//                            .header("Content-Type", "application/json");
//                    request.body(jsonStr)
//                            .execute().body();
                    return this.getSuccessModel(wenDaBody.getContent());
                }
            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(wenDaBody);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    @PostMapping({"/api/getWendaContent/zhonghang"})
    public ResultModel getWendaContentZhonghang(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            String testStr = wenDaParam.getPrompt();
            String result = KeywordStrategyFactory.checkPrompt(testStr);
            log.info("getWendaContent str:{}",testStr);
            //判断testStr长度是否大于3 如果是 则继续执行,否则直接返回
            if (testStr.length() < 3) {
                return this.getErrorModel("你的问题我没有听清");
            }
            String prompt = wenDaParam.getPrompt();
            log.info("getWendaContentV4 str:{}",prompt);
            WenDaBody wenDaBody = null;
            Map resultJson = new HashMap();
            resultJson.put("tag","javaApi");
            if (StrUtil.isNotEmpty(result) ) {
                if (!result.equals("其他"))
                    prompt = result;
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (result.equals("你好")|| result.equals("小元")) {
                    redisTemplate.opsForValue().set(questionWaitingKey+ wenDaParam.getSession_id(), maxQuestionWaitingCount, 15, TimeUnit.SECONDS);
//                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"你好，我是雄安兴元的数字人小元,请说小元来唤醒我。\"}";
                    //调用传参
//                    HttpRequest request  = HttpRequest.post(aivtUrl)
//                            .header("Content-Type", "application/json");
//                    request.body(jsonStr)
//                            .execute().body();
                    resultJson.put("type","wenda_Hello");
                    resultJson.put("text","你好呀,我是中行数字人,请问有什么我可以帮您");
                    log.info("getWendaContentV4 str:{}",result);
                    return this.getSuccessModel(new Gson().toJson(resultJson));
                }
                //判断是否存在redis
//                if (!redisTemplate.hasKey(questionWaitingKey)) {
//                    //如果存在,则返回
//                    return this.getErrorModel("没有激活小元,请先唤醒小元");
//                }
                //收集问题并存入mysql中
                BankQuestionModel bankQuestionModel = new BankQuestionModel();
                bankQuestionModel.setQuestion(removeEmoji(prompt));
                bankQuestionModel.setCreateDate(new Date());
                bankQuestionService.insertQuestion(bankQuestionModel);
                log.info("prompt---------:{}",prompt);
                wenDaParam.setPrompt(prompt);
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    //调用失败则再次调用本地大模型接口: 127.0.0.1:17860/chat  {"prompt":"测试传输","keyword":"测试传输","temperature":0.8,"top_p":0.8,"max_length":4096,"history":[]}
                    SimpleSemanticRequest request = new SimpleSemanticRequest();
                    request.setQuestion(prompt);
                    request.setThreshold(0.5d);
                    SimpleSemanticResult matchResult = semanticMatchService.simpleMatch(request);
                    log.info("getWendaContentV4 str:{}",request.toString());
                    resultJson.put("type","wenda_chat");
                    resultJson.put("text",matchResult);
                    log.info("getWendaContentV4 str:{}",result);
                    return this.getSuccessModel(new Gson().toJson(resultJson));
                }else {
                    //删除questionWaitingKey
                    if (redisTemplate.hasKey(questionWaitingKey+ wenDaParam.getSession_id())) {
                        redisTemplate.delete(questionWaitingKey+ wenDaParam.getSession_id());
                    }
                    //调用成功传参
                    resultJson.put("type","wenda_rag");
                    resultJson.put("text",wenDaBody.getContent());
                    log.info("getWendaContentV4 str:{}",wenDaBody.getContent());
                    return this.getSuccessModel(new Gson().toJson(resultJson));
                }
            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(wenDaBody);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }
    public String removeEmoji(String input) {
        return input.replaceAll("[^\\u0000-\\uFFFF]", ""); // 移除非基本平面字符（如 Emoji）
    }

    @PostMapping({"/api/getWendaContent/zhonghang/active"})
    public ResultModel getWendaContentZhonghangActice(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getWendaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            String testStr = wenDaParam.getPrompt();
            String result = KeywordStrategyFactory.checkPrompt(testStr);
            log.info("getWendaContent str:{}",testStr);
            String prompt = wenDaParam.getPrompt();
            log.info("getWendaContentV4 str:{}",prompt);
            WenDaBody wenDaBody = null;
            Map resultJson = new HashMap();
            resultJson.put("tag","javaApi");
            if (StrUtil.isNotEmpty(result) ) {
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (result.equals("小元")) {
                    redisTemplate.opsForValue().set(questionWaitingKey + wenDaParam.getSession_id(), maxQuestionWaitingCount, 8, TimeUnit.SECONDS);
                    resultJson.put("type","wenda_Hello");
                    resultJson.put("text","你好呀");
                    log.info("getWendaContentV4 str:{}",result);
                    return this.getSuccessModel(new Gson().toJson(resultJson));
                }
                //判断是否存在redis
                if (!redisTemplate.hasKey(questionWaitingKey)) {
                    //如果存在,则返回
                    return this.getErrorModel("没有激活小元,请先唤醒小元");
                }
            }
            Gson gson = new Gson();
            String jsonStr = gson.toJson(resultJson);
            resultJson.put("type","wenda_Hello");
            resultJson.put("text",result);
            return this.getSuccessModel(jsonStr);
        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    //智力问答接口
    @PostMapping({"/api/getWendaContent/ceyan"})
    public ResultModel getZhiLiWenDaContent(@RequestBody WenDaParam wenDaParam) {
        try {
            log.info("getZhiLiWenDaContent str:{}",wenDaParam);
            if (ObjectUtil.isEmpty(wenDaParam)) {
                return this.getErrorModel("参数为空2");
            }
            //获取问题
            List<AnswerModel> answerModels = readJson();
            String testStr = wenDaParam.getPrompt();
            String result = KeywordStrategyFactory.checkPrompt(testStr);
            //判断result 是否为 测验 如果是 则查询知识库
            if (result.equals("测验")) {
                redisTemplate.opsForValue().set(currentCountKey, -70);
                redisTemplate.opsForValue().set(questionWaitingKey, maxQuestionWaitingCount, 15, TimeUnit.SECONDS);
                log.info("pick success , result:{}",testStr,result);
                String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"ceyan.mp4\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":0,\"interrupt\":true}";
                //调用失败传参
                HttpRequest request = HttpRequest.post(aiVideoUrl)
                        .header("Content-Type", "application/json");
                request.body(jsonStr)
                        .execute().body();
                //随机抽取3个问题并放入redis的set中
                if (ObjectUtil.isEmpty(answerModels)) {
                    return this.getErrorModel("没有找到问题");
                }
                //随机抽取3个问题
                //将问题放入redis的中
                //清除questionSetKey
                if (redisTemplate.hasKey(questionSetKey)) redisTemplate.delete(questionSetKey);
                for (int i = 0; i < 3; i++) {
                    int randomIndex = (int) (Math.random() * answerModels.size());
                    AnswerModel answerModel = answerModels.get(randomIndex);
                    redisTemplate.opsForList().leftPush(questionSetKey, answerModel.getVideoName());
                    redisTemplate.opsForList().leftPush(questionSetKey, answerModel.getAnswer());
                }
                //推送第一个问题
                String firstQuestion = (String) redisTemplate.opsForList().rightPop(questionSetKey);
                String firstQuestionJson = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"" + firstQuestion+" \",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                //调用失败传参
                HttpRequest requestQ = HttpRequest.post(aiVideoUrl)
                        .header("Content-Type", "application/json");
                requestQ.body(firstQuestionJson)
                        .execute().body();
                //开始计时 30s
                redisTemplate.opsForValue().set(limitCountKey, -10);
                //根据firstQuestion匹配answerModels中的question
                //保证格式
                String question = answerModels.stream().filter(answerModel -> answerModel.getVideoName().equals(firstQuestion)).findFirst().get().getQuestion();

                //推送前端展示文字
                return this.getSuccessModel(question+",请回答正确或错误");
            }
            //判断是否存在 正确 错误
            if (result.equals("正确") || result.equals("错误")) {
                redisTemplate.opsForValue().set(questionWaitingKey, maxQuestionWaitingCount, 15, TimeUnit.SECONDS);
                //取出redis的List中的答案
                String answerResult = (String) redisTemplate.opsForList().rightPop(questionSetKey);
                if (result.contains(answerResult)) {
                    //出栈下一个问题
                    String nextQuestion = (String) redisTemplate.opsForList().rightPop(questionSetKey);
                    if (StrUtil.isEmpty(nextQuestion)) {
                        //如果没有下一个问题,则提示完毕
                        String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"answerComplete.mp4\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                        //调用失败传参
                        HttpRequest request = HttpRequest.post(aiVideoUrl)
                                .header("Content-Type", "application/json");
                        request.body(jsonStr)
                                .execute().body();
                        //删除questionSetKey
                        redisTemplate.delete(limitCountKey);
                        //延时5s推送

                        return this.getSuccessModel("恭喜您,答题完成");
                    } else {
                        //如果是正确或者错误,则直接调用视频
                        String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"answerRight.mp4\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                        //调用失败传参
                        HttpRequest request = HttpRequest.post(aiVideoUrl)
                                .header("Content-Type", "application/json");
                        request.body(jsonStr)
                                .execute().body();
                        //开始计时 30s
                        redisTemplate.opsForValue().set(limitCountKey, -10);
                        String nextQuestionJson = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"" + nextQuestion+" \",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                        //如果有下一个问题,则推送下一个问题
                        HttpRequest requestN = HttpRequest.post(aiVideoUrl)
                                .header("Content-Type", "application/json");
                        requestN.body(nextQuestionJson)
                                .execute().body();
                        //保证格式
                        String question = answerModels.stream().filter(answerModel -> answerModel.getVideoName().equals(nextQuestion)).findFirst().get().getQuestion();
                        //延时5s推送

                        return this.getSuccessModel(question+",请回答正确或错误");
                    }
                }else {
                    //如果是正确或者错误,则直接调用视频
                    String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\"answerError.mp4\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                    //调用失败传参
                    HttpRequest request = HttpRequest.post(aiVideoUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    //删除questionSetKey
                    redisTemplate.delete(questionSetKey);
                    //删除limitCountKey
                    redisTemplate.delete(limitCountKey);
                }
                log.info("pick success , result:{}",testStr,result);
                //延时5s推送

                return this.getSuccessModel("回答错误,期待您的下次挑战");
            }
            return this.getErrorModel(result);

        } catch (Exception e) {
            return this.getErrorModel(e.getMessage());
        }
    }

    public List<AnswerModel> readJson() throws IOException {
        // 1. 定位资源（路径从 resources 根目录开始）
        ClassPathResource resource = new ClassPathResource("json/data.json");

        // 2. 获取输入流（关键！避免直接使用 File 对象）
        try (InputStream inputStream = resource.getInputStream()) {
            // 3. 读取为字符串
            String jsonContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

            // 4. 解析为对象（以 List 为例）
            ObjectMapper objectMapper = new ObjectMapper();
            //使用Gson
            Gson gson = new Gson();
            List<AnswerModel> answerModels = gson.fromJson(jsonContent, new TypeReference<List<AnswerModel>>(){}.getType());
            // 5. 返回结果
            return answerModels;
        }catch (Exception e) {
            log.error("读取JSON文件失败: {}", e.getMessage());
            return new ArrayList<>(); // 返回空列表或处理异常
        }
    }

    //提供模型名称接口
    @GetMapping("/v1/models")
    public Map getModels() {
        //创建一个Map对象
        Map<String, Object> map = new HashMap<>();
        //"object": "list",
        //        "data": [
        //            {
        //                "id": "chatglm3-6b",  # 这个要与你在 Dify 配置中填写的 model 名一致
        //                "object": "model",
        //                "created": 1699999999,
        //                "owned_by": "chatglm-local"
        //            }
        //        ]
        map.put("object", "list");
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> model = new HashMap<>();
        model.put("id", "Wenda");  // 这个要与你在 Dify 配置中填写的 model 名一致
        model.put("object", "model");
        model.put("created", System.currentTimeMillis() / 1000);
        model.put("owned_by", "chatglm-local");
        data.add(model);
        map.put("data", data);
        return map;
    }


    //基于openAI格式调用闻达大模型
    @PostMapping("/v1/chat/completions")
    public Map chatToWenda(@RequestBody OpenAIModelParam request) {
        log.info("chatToWenda , request:{}", request);
        //获取request中的messages
        List<OpenAIMessage> messages = request.getMessages();
        String msg = "";
        for (OpenAIMessage message : messages) {
            //判断message的role是否为user
            if ("user".equals(message.getRole())) {
                //获取content
                String content = message.getContent();
                msg = msg + content;
            }
        }
        //创建WenDaParam对象
        HttpRequest requestWenda = HttpRequest.post(aiWendaUrl)
                .header("Content-Type", "application/json");

        String sendStr = "{\"prompt\":\"你是智能百科,每个问题尽量不超过20字,回答内容不要带格式,问题如下:"+msg+"\",\"keyword\":\"你是智能百科,每个问题尽量不超过20字,回答内容不要带格式,问题如下:"+msg+"\",\"temperature\":0.8,\"top_p\":0.8,\"max_length\":4096,\"history\":[]}";
        String result = requestWenda.body(sendStr)
                .execute().body();
        Map<String, Object> map = new HashMap<>();
        map.put("id", "chatcmpl-"+ System.currentTimeMillis());
        map.put("object", "chat.completion");
        map.put("created", System.currentTimeMillis() / 1000);
        map.put("model", "Wenda");
        List<Map> choices = new ArrayList<>();
        Map choice = new HashMap();
        choice.put("delta", new HashMap<String, String>() {{
            put("content", result);
        }});
        choice.put("finish_reason", "stop");
        choices.add(choice);
        map.put("choices", choices);
        return map;

    }
    //调用dify大模型
    @PostMapping("/v1/chat/completions/dify")
    public Map chatToDify(@RequestBody OpenAIModelParam aiRequest) throws IOException{
        log.info("chatToDify , request:{}", aiRequest);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)   // 连接超时
                .readTimeout(0, TimeUnit.SECONDS)       // 读取超时（0 表示永不超时，适用于 SSE）
                .writeTimeout(10, TimeUnit.SECONDS)     // 写入超时
                .build();
        //获取request中的messages
        List<OpenAIMessage> messages = aiRequest.getMessages();
        String msg = "";
        for (OpenAIMessage message : messages) {
            //判断message的role是否为user
            if ("user".equals(message.getRole())) {
                //获取content
                String content = message.getContent();
                msg = content;
            }
        }
        Map <String, Object> data = new HashMap<>();
        data.put("inputs", new HashMap<>());
        data.put("query", msg);
        data.put("response_mode", "streaming");
        data.put("conversation_id", "");
        data.put("user", "abc-123");
        data.put("is_retry ",false);
        //如果有文件，则添加到files中
        List<Map<String, Object>> files = new ArrayList<>();
        data.put("files", files);
        //调用dify大模型接口
        Request request = new Request.Builder()
                .url(aiDifyChatMessagesUrl)
                .header("Authorization", aiDifyKey)
                .header("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(new Gson().toJson(data), MediaType.parse("application/json")))
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        //判断response是否为null
        Map<String, Object> map = new HashMap<>();
        map.put("id", "chatcmpl-"+ System.currentTimeMillis());
        map.put("object", "chat.completion");
        map.put("created", System.currentTimeMillis() / 1000);
        map.put("model", "Wenda");
        List<Map> choices = new ArrayList<>();
        Map choice = new HashMap();
        //判断postResult 中 的status是否为200
        if (response.code() != 200) {
            choice.put("delta", new HashMap<String, String>() {{
                put("content", "模型调用失败，请稍后再试或联系管理员。");
            }});
            choice.put("finish_reason", "stop");
            choices.add(choice);
            map.put("choices", choices);
            return map;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        String line;
        StringBuilder fullAnswer = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("data:")) continue;

            String jsonLine = line.substring(5).trim();
            JsonObject obj = JsonParser.parseString(jsonLine).getAsJsonObject();
            String event = obj.get("event").getAsString();

            if ("message".equals(event)) {
                String fragment = obj.get("answer").getAsString();
                fullAnswer.append(fragment);
            }

            if ("workflow_finished".equals(event)) {
                JsonObject outputs = obj.getAsJsonObject("data").getAsJsonObject("outputs");
                if (outputs != null && outputs.has("answer")) {
                    String finalAnswer = outputs.get("answer").getAsString();
                    System.out.println("Final Answer from outputs: " + finalAnswer);
                } else {
                    System.out.println("No final answer in workflow_finished");
                }
            }
        }
        //将result中的 message提取出来
        String result = String.valueOf(fullAnswer);
        log.info("chatToDify result:{}", result);
        choice.put("delta", new HashMap<String, String>() {{
            put("content", result);
        }});
        choice.put("finish_reason", "stop");
        choices.add(choice);
        map.put("choices", choices);
        return map;
    }


    public static List<String> extractBracesContent(String input) {
        List<String> matches = new ArrayList<>();
        // 正则表达式匹配{}中的内容
        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(input);
        // 遍历所有匹配项
        while (matcher.find()) {
            // 提取捕获组1的内容（即大括号内的部分）
            matches.add(matcher.group(1));
        }
        return matches;
    }

    private ResultModel getErrorModel(String str){
        ResultModel model=new ResultModel();
        model.setCode(500);
        model.setResultStr(str);
        return model;
    }

    private ResultModel getSuccessModel(String str){
        ResultModel model=new ResultModel();
        model.setCode(200);
        model.setResultStr(str);
        return model;
    }

}
