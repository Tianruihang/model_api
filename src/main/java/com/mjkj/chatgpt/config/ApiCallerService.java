package com.mjkj.chatgpt.config;
import com.mjkj.chatgpt.service.BankQuestionService;
import com.mjkj.chatgpt.service.IChatGPTService;
import com.mjkj.chatgpt.service.QuestionAnalysisService;
import com.mjkj.chatgpt.service.QuestionStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class ApiCallerService {
    // API调用间隔（秒）
    @Value("${api.caller.interval:30}")
    private int interval;
    @Autowired
    private IChatGPTService chatGPTService;
    @Autowired
    private QuestionAnalysisService analysisService;
    @Autowired
    private QuestionStatisticsService statisticsService;
    @Autowired
    RedisTemplate redisTemplate;

    private final String limitCountKey = "api:limitCount"; // Redis中存储限制调用次数的键
    private final String currentCountKey = "api:currentCount"; // Redis中存储当前播放视频下标（0-6）
    private final String questionSetKey="ceyan:questions";
    private final String videoIndexKey = "api:videoIndex"; // 顺序播放的视频索引（1-7）
    /**
     * 定时调用API的方法
     */
    @Scheduled(fixedRateString = "${apiCallerProperties.interval:1}000")
    public void callApiTask() {
        if (!redisTemplate.hasKey(currentCountKey)){
            // 如果Redis中没有当前计数的键，则初始化为1
            redisTemplate.opsForValue().set(currentCountKey, 1);
            log.info("Redis中没有当前计数的键，已初始化为1");
        } else {
            int counter =  (int) redisTemplate.opsForValue().get(currentCountKey);
            //判断当前计数是否超过最大值
            if (counter >= 40 ) {
                // 初始化或获取顺序播放索引
                if (!redisTemplate.hasKey(videoIndexKey)) {
                    redisTemplate.opsForValue().set(videoIndexKey, 1);
                }
                int currentVideoIndex = (int) redisTemplate.opsForValue().get(videoIndexKey);
                String num = "explain" + currentVideoIndex + ".mp4";
                boolean boo = chatGPTService.pushVideoToFront(num);
                if (boo) {
                    // 播放成功后，索引顺序递增并循环到1-7
                    int nextVideoIndex = currentVideoIndex >= 7 ? 1 : currentVideoIndex + 1;
                    redisTemplate.opsForValue().set(videoIndexKey, nextVideoIndex);
                    // 如果API调用成功，重置计数器
                    redisTemplate.opsForValue().set(currentCountKey, -40);
                    log.info("API调用成功，顺序播放视频: {}，下一个索引: {}", num, nextVideoIndex);
                } else {
                    counter++;
                    redisTemplate.opsForValue().set(currentCountKey, counter);
                }
            } else {
                counter++;
                redisTemplate.opsForValue().set(currentCountKey, counter);
                log.info("当前计数为: " + counter + "，未达到调用API的条件");
            }
        }
        // 获取限制调用次数
        if (redisTemplate.hasKey(limitCountKey)){
            int counter = (int) redisTemplate.opsForValue().get(limitCountKey);
            if (counter >= 30){
               chatGPTService.pushVideoToFront("answerError.mp4");
                // 如果API调用成功，重置计数器
                redisTemplate.opsForValue().set(currentCountKey, 0);
                //清除limitCountKey
                redisTemplate.delete(limitCountKey);
                //删除questionSetKey
                redisTemplate.delete(questionSetKey);
            }else {
                counter++;
                redisTemplate.opsForValue().set(limitCountKey, counter);
            }

        }
    }

    /**
     *  定时归档
     */
//    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜归档
//    public void archiveData() {
//        // 归档逻辑
//        log.info("开始归档数据...");
//        analysisService.processAllQuestions();
//        log.info("数据归档完成");
//    }
//    /**
//     * 定时统计 定时归档结束后10分钟执行
//     */
//    @Scheduled(cron = "0 10 0 * * ?") // 每天午夜归档后10分钟统计
//    public void generateStatistics() {
//        log.info("开始生成统计数据...");
//        statisticsService.generateAndSaveStatistics(new java.util.Date());
//        log.info("统计数据生成完成");
//    }

}
