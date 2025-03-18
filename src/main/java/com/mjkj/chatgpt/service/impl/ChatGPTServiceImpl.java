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
package com.mjkj.chatgpt.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.mjkj.chatgpt.model.ChatGptParam;
import com.mjkj.chatgpt.model.ChatGptPublicParam;
import com.mjkj.chatgpt.service.IChatGPTService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * chatgpt相关
 */
@Slf4j
@Service
public class ChatGPTServiceImpl implements IChatGPTService {

    @Autowired
    RedisTemplate redisTemplate;

    //获取GPT内容
    @Override
    public String getChatContent(ChatGptParam gptModel) {
        String url = gptModel.getUrl();
        String apiKey = gptModel.getKey();
        String model = gptModel.getModel();//text-davinci-003
        String prompt = gptModel.getPrompt();//输入的文本提示 必填

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("model", model);
        paramMap.put("prompt", prompt);

        if (ObjectUtil.isNotEmpty(gptModel.getTemperature())) {//控制生成文本的多样性
            paramMap.put("temperature", gptModel.getTemperature());
        }

        if (ObjectUtil.isNotEmpty(gptModel.getMax_tokens())) {//控制生成文本的长度
            paramMap.put("max_tokens", gptModel.getMax_tokens());
        }
        if (ObjectUtil.isNotEmpty(gptModel.getTop_p())) {////控制生成文本的多样性
            paramMap.put("top_p", gptModel.getTop_p());
        }

       String jsonStr = JSONUtil.toJsonStr(paramMap);

        HttpRequest request  = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey);
        String body =/*request.setHttpProxy("127.0.0.1", 7890)*/
        request.body(jsonStr)
                .execute().body();
        log.info("****** body *****" + body);
        return body;
    }

    //获取GPT内容
    @Override
    public String getChatGptPublic(ChatGptPublicParam param) {
        String url = param.getUrl();
        String apiKey = param.getKey();
        String req_body = param.getBody();


        HttpRequest request  = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey);
        HttpResponse response = request.body(req_body).execute();
		String body =/*request.setHttpProxy("127.0.0.1", 7890)*/
        request.body(req_body)
                .execute().body();
        log.info("****** body *****" + body);
        return body;
    }

    @Override
    public String getWenxinContent(ChatGptPublicParam param) {
        String req_body = param.getBody();
        String apiUrl = param.getUrl();
        String apiKey = param.getKey();

        String oauthToken = getOauthToken();
        log.info("****** oauthToken *****" + oauthToken);
        // 构造请求参数和头信息
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 连接超时时间
                .readTimeout(60, TimeUnit.SECONDS)    // 读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)   // 写入超时时间
                .build();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        Gson gson = new Gson();
        Map map = gson.fromJson(req_body, Map.class);
        List<Map> list = (List<Map>) map.get("messages");
        Map<String, Object> requestMap = new HashMap<>();
        List<Map<String, String>> messagesList = new ArrayList<>();
        messagesList.add(list.get(1)); // 添加单个消息对象到数组
        // 将 messagesList 放入 requestMap
        requestMap.put("messages", messagesList);
        String jsonRequestBody = gson.toJson(requestMap);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonRequestBody);

        Request request = new Request.Builder()
                .url(apiUrl + "?access_token=" + oauthToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        log.info("****** request *****" + request);
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();
            log.info("****** responseBody *****" + responseBody);
            // 解析响应结果，返回用户需要的部分
            return parseResponse(responseBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseResponse(String responseBody) {
        // 这里需要根据实际返回的JSON格式进行解析
        Gson gson = new Gson();
        Map map = gson.fromJson(responseBody, Map.class);
        return map.get("result").toString();
    }

    private String getOauthToken() {
        //查询redis中是否有token
        String token = (String) redisTemplate.opsForValue().get("baidu_token");
        if (token != null) {
            return token;
        }
        // 构造请求参数和头信息
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, "{\"grant_type\":\"client_credentials\"}");

        Request request = new Request.Builder()
                .url("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + "oWKeFK0tdhhSEQXqFRg8pi4F" + "&client_secret=" + "4p87VWrjuVW84dwFzy416xbDxS9REjhz")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            Gson gson = new Gson();
            Map map = gson.fromJson(response.body().string(), Map.class);
            //将token过期时间放redis内
            //expires_in: 2592000.0
            String expires_in = map.get("expires_in").toString();
            Long expireTime = Long.parseLong(expires_in.split("\\.")[0]);
            redisTemplate.opsForValue().set("baidu_token", map.get("access_token"), expireTime, TimeUnit.SECONDS);
            // 解析响应结果，返回用户需要的部分
            return map.get("access_token").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
