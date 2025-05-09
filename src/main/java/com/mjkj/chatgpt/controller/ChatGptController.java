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
import com.google.gson.Gson;
import com.mjkj.chatgpt.model.*;
import com.mjkj.chatgpt.service.IChatGPTService;
import com.mjkj.chatgpt.service.WenDaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@RestController
@CrossOrigin(origins = "*", allowCredentials = "true")
@RequestMapping("chatgpt")
public class ChatGptController {

    @Autowired
    private IChatGPTService chatGPTService;
    @Autowired
    private WenDaService wenDaService;
    @Value("${config.aivt.url:http://120.211.84.149:8082/send}")
    private String aivtUrl;
    @Value("${config.ai.wenda.url:http://120.211.84.149:17860/api/chat}")
    private String aiWendaUrl;
    @Value("${config.ai.video.url:http://127.0.0.1:8091/show/local")
    private String aiVideoUrl;


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
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    //调用失败则再次调用本地大模型接口: 127.0.0.1:17860/chat  {"prompt":"测试传输","keyword":"测试传输","temperature":0.8,"top_p":0.8,"max_length":4096,"history":[]}
                    String sendStr = "{\"prompt\":\"你是智能百科,每个问题尽量不超过20字,问题如下:"+prompt+"\",\"keyword\":\"你是智能百科,每个问题尽量不超过20字,问题如下:"+prompt+"\",\"temperature\":0.8,\"top_p\":0.8,\"max_length\":4096,\"history\":[]}";
                    log.info("getWendaContentV4 str:{}",sendStr);
                    HttpRequest requestWenda = HttpRequest.post(aiWendaUrl)
                            .header("Content-Type", "application/json");
                    String result = requestWenda.body(sendStr)
                            .execute().body();
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

    @PostMapping({"/api/getWendaContent/zhonghang"})
    public ResultModel getWendaContentZhonghang(@RequestBody WenDaParam wenDaParam) {
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
            log.info("getWendaContentV4 str:{}",prompt);
            WenDaBody wenDaBody = null;
            if (StrUtil.isNotEmpty(prompt) ) {
                //如果包含 唤醒小元 则发送 你好,我是小元,请问有什么需要帮助的
                if (prompt.contains("唤醒小元")) {
                    String jsonStr = "{\"type\":\"reread\",\"platform\":\"webui\",\"username\":\"游客\",\"content\":\"你好，我是中行的数字人小元,请说小元来唤醒我。\"}";
                    //调用传参
                    HttpRequest request  = HttpRequest.post(aiVideoUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }
                wenDaBody  = wenDaService.getWenDaContent(wenDaParam);
                if (ObjectUtil.isEmpty(wenDaBody)) {
                    //调用失败则再次调用本地大模型接口: 127.0.0.1:17860/chat  {"prompt":"测试传输","keyword":"测试传输","temperature":0.8,"top_p":0.8,"max_length":4096,"history":[]}
                    String result = "error.MP4";
                    String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\""+result+"\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                    log.info("getWendaContentV4 str:{}",jsonStr);
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aiVideoUrl)
                            .header("Content-Type", "application/json");
                    request.body(jsonStr)
                            .execute().body();
                    return this.getSuccessModel("成功推送");
                }else {
                    //调用成功传参
                    String jsonStr = "{\"type\":\"easy_wav2lip\",\"video_path\":{\"path\":\""+extractBracesContent(wenDaBody.getContent())+"\",\"format\":\"mp4\"},\"audio_path\":\"baidu_9.wav\",\"insert_index\":-1}";
                    //调用失败传参
                    HttpRequest request  = HttpRequest.post(aiVideoUrl)
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
