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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("chatgpt")
public class ChatGptController {

    @Autowired
    private IChatGPTService chatGPTService;
    @Autowired
    private WenDaService wenDaService;
    @Value("${config.aivt.url:http://127.0.0.1:8082/send}")
    private String aivtUrl;

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
