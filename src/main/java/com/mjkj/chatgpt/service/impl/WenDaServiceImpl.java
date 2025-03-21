package com.mjkj.chatgpt.service.impl;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mjkj.chatgpt.model.WenDaBody;
import com.mjkj.chatgpt.model.WenDaParam;
import com.mjkj.chatgpt.service.WenDaService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WenDaServiceImpl implements WenDaService {

    @Value("${config.max.scores:180}")
    private int maxScores;
    @Value("${config.wenda.step:5}")
    private String stepWenda;
    //memory_name
    @Value("${config.wenda.memory.name:default}")
    private String memoryName;
    //url
    @Value("${config.wenda.url:http://127.0.0.1:17860/api/find_rtst_in_memory}")
    private String urlWenda;

    @Override
    public WenDaBody getWenDaContent(WenDaParam wenDaParam) {
        String url = urlWenda;
        String prompt = wenDaParam.getPrompt();
        String step = stepWenda;
        String memory_name = memoryName;
        String jsonStr = "{\"prompt\":\"" + prompt + "\",\"step\":" + step + ",\"memory_name\":\"" + memory_name + "\"}";
        HttpRequest request  = HttpRequest.post(url)
                .header("Content-Type", "application/json");
        request.body(jsonStr)
                .execute().body();
        String body = request.body(jsonStr)
                        .execute().body();
        //
        log.info("****** body *****" + body);
        //将body转为List<WenDaBody>
        Gson gson = new Gson();
        Type listType = new TypeToken<List<WenDaBody>>() {}.getType();
        // 将 JSON 字符串解析为 List<WenDaBody>
        List<WenDaBody> wenDaBodyList = gson.fromJson(body, listType);
        // 遍历 List<WenDaBody> 并找出wenDaBody最少的score中的值,
        int minScore = maxScores;
        // 使用 Stream API 找到最小 score 的对象
        Optional<WenDaBody> minScoreBody = wenDaBodyList.stream()
                .min((body1, body2) -> Integer.compare(body1.getScore(), body2.getScore()));
        if (minScoreBody.isPresent()) {
            WenDaBody wenDaBody = minScoreBody.get();
            // 获取最小 score 的值
            minScore = wenDaBody.getScore();
            log.info("****** minScore *****" + minScore);
            // 获取最小 score 对应的对象
            log.info("****** wenDaBody *****" + wenDaBody);
            if (minScore<= maxScores) {
                // 处理最小 score 对应的对象
                log.info("****** wenDaBody *****" + wenDaBody);
                return wenDaBody;
            }
        }
        return null;
    }
}
