package com.mjkj.chatgpt.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mjkj.chatgpt.config.NavigationProperties;
import com.mjkj.chatgpt.model.NavStatus;
import com.mjkj.chatgpt.service.ConfigService;
import com.mjkj.chatgpt.service.NavigationManagerService;
import com.mjkj.chatgpt.service.NavigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class NavigationManagerServiceImpl implements NavigationManagerService {
    private final NavigationService navigationService;
    private final NavigationProperties navigationProperties;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private ConfigService configService;
    
    private Timer timer;
    private List<NavigationProperties.NavigationItem> destinations;
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean waitingForNextDestination = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final AtomicBoolean singleNavigation = new AtomicBoolean(false);
    private String singleDestination;
    private boolean wasRunningBeforeSingleNav = false;

    private static final String PUSH_API_URL = "http://127.0.0.1:5010/api/push";
    private static final String AUDIO_DRIVE_API_URL = "http://127.0.0.1:8010/audio_drive";
    private static final long STATUS_CHECK_INTERVAL = 1000;
    private static final long INITIAL_DELAY = 10000;
    private static final String SESSION_ID_KEY = "BOBAOKEY";
    private static final String DEFAULT_SESSION_ID = "nav_session";

    @Autowired
    public NavigationManagerServiceImpl(NavigationService navigationService, NavigationProperties navigationProperties) {
        this.navigationService = navigationService;
        this.navigationProperties = navigationProperties;
    }

    @Override
    public void startNavigationLoop(List<String> destinations) {
        if (running.get()) {
            log.warn("导航循环已经在运行中");
            return;
        }

        if (destinations == null || destinations.isEmpty()) {
            log.error("导航地点列表不能为空");
            return;
        }

        List<NavigationProperties.NavigationItem> items = IntStream.range(0, destinations.size())
                .mapToObj(i -> new NavigationProperties.NavigationItem(i + 1, destinations.get(i), 5))
                .collect(Collectors.toList());
        this.destinations = items;
        currentIndex.set(0);
        running.set(true);
        waitingForNextDestination.set(false);
        paused.set(false);
        log.info("开始执行下一个点导航");
        startNextNavigation();
    }

    public void startNavigationLoopFromConfig() {
        if (running.get()) {
            log.warn("导航循环已经在运行中");
            return;
        }

        List<NavigationProperties.NavigationItem> configDestinations = navigationProperties.getNavigationItems();
        if (configDestinations == null || configDestinations.isEmpty()) {
            log.error("配置文件中没有导航地点");
            return;
        }

        this.destinations = configDestinations;
        currentIndex.set(0);
        running.set(true);
        waitingForNextDestination.set(false);
        paused.set(false);

        log.info("从配置文件加载导航地点: {}", configDestinations.stream()
                .map(item -> item.getDestination() + "(" + item.getDelayMinutes() + "分钟)")
                .collect(java.util.stream.Collectors.toList()));

        startNextNavigation();
    }

    private void startNextNavigation() {
        if (!running.get()) {
            return;
        }

        NavigationProperties.NavigationItem item = destinations.get(currentIndex.get());
        String destination = item.getDestination();
        log.info("开始导航到第 {} 个地点: {}，等待时间: {} 分钟", 
                currentIndex.get() + 1, destination, item.getDelayMinutes());
        
        navigationService.startNavigation(destination);
        waitingForNextDestination.set(false);
        //等10秒后检查导航状态
        startStatusCheckTimer();
    }

    private void startStatusCheckTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!running.get()) {
                    timer.cancel();
                    return;
                }

                if (paused.get()) {
                    return;
                }

                NavStatus status = navigationService.getNavStatus();
                String currentDestination = destinations.get(currentIndex.get()).getDestination();
                log.debug("导航状态: res={}, goal={}, dist={}, mileage={}, 当前目标地点={}", 
                        status.getRes(), status.getGoal(), status.getDist(), status.getMileage(), currentDestination);

                if (status.getRes() == 3 && currentDestination.equals(status.getGoal())) {
                    timer.cancel();
                    handleNavigationComplete(status.getGoal());
                }
            }
        }, INITIAL_DELAY, STATUS_CHECK_INTERVAL);
    }

    private void handleNavigationComplete(String goal) {
        log.info("导航完成，到达地点: {}", goal);
        // pushToApi(text);
        //推送音频
        pushAudioDrive(goal);
        
        scheduleNextNavigation();
    }
    
    private void pushAudioDrive(String goal) {
        try {
            String sessionId = getSessionIdFromCache();
            
            String audioPath = findAudioPathByDestination(goal);
            if (audioPath == null) {
                log.warn("未找到地点 {} 的音频路径配置，使用默认路径", goal);
                audioPath = "/audio/" + goal + ".wav";
            }
            
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("sessionid", sessionId);
            jsonMsg.put("audio_path", audioPath);
            jsonMsg.put("avatar_id", "wy1");
            
            log.debug("推送音频请求: {}", jsonMsg.toJSONString());
            String response = HttpRequest.post(AUDIO_DRIVE_API_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonMsg.toJSONString())
                    .execute()
                    .body();
            
            log.info("推送音频驱动成功，音频路径: {}, 响应: {}", audioPath, response);
        } catch (Exception e) {
            log.error("推送音频驱动失败: {}", e.getMessage());
        }
    }
    
    private String findAudioPathByDestination(String destination) {
        if (destinations != null) {
            String path = destinations.stream()
                    .filter(item -> destination.equals(item.getDestination()))
                    .map(NavigationProperties.NavigationItem::getAudioPath)
                    .findFirst()
                    .orElse(null);
            if (path != null) {
                return path;
            }
        }
        
        List<NavigationProperties.NavigationItem> configItems = navigationProperties.getNavigationItems();
        if (configItems != null) {
            return configItems.stream()
                    .filter(item -> destination.equals(item.getDestination()))
                    .map(NavigationProperties.NavigationItem::getAudioPath)
                    .findFirst()
                    .orElse(null);
        }
        
        return null;
    }

    private void pushToApi(String text) {
        try {
            String sessionId = getSessionIdFromCache();
            
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("type", "echo");
            jsonMsg.put("text", text);
            jsonMsg.put("insert_index", 0);
            jsonMsg.put("interrupt", true);
            jsonMsg.put("sessionId", sessionId);

            String response = HttpRequest.post(PUSH_API_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonMsg.toJSONString())
                    .execute()
                    .body();

            log.info("推送消息到API成功: {}", response);
        } catch (Exception e) {
            log.error("推送消息到API失败: {}", e.getMessage());
        }
    }
    
    private String getSessionIdFromCache() {
        try {
            if (redisTemplate != null && redisTemplate.hasKey(SESSION_ID_KEY)) {
                String sessionId = redisTemplate.opsForValue().get(SESSION_ID_KEY);
                if (sessionId != null && !sessionId.isEmpty()) {
                    log.debug("从缓存获取sessionId成功: {}", sessionId);
                    return sessionId;
                }
            }
        } catch (Exception e) {
            log.warn("从缓存获取sessionId失败: {}", e.getMessage());
        }
        log.debug("使用默认sessionId: {}", DEFAULT_SESSION_ID);
        return DEFAULT_SESSION_ID;
    }

    private void scheduleNextNavigation() {
        waitingForNextDestination.set(true);
        
        long delayMillis = destinations.get(currentIndex.get()).getDelayMillis();
        
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (!running.get()) {
                    return;
                }

                if (currentIndex.get() >= destinations.size() - 1) {
                    log.info("已完成所有地点导航，导航到充电桩");
                    navigationService.startNavigation("充电桩");
                    running.set(false);
                    return;
                }

                currentIndex.set(currentIndex.get() + 1);
                startNextNavigation();
            }
        }, delayMillis);

        log.info("等待{}分钟后导航到下一个地点...", destinations.get(currentIndex.get()).getDelayMinutes());
    }

    @Override
    public void stopNavigationLoop() {
        running.set(false);
        if (timer != null) {
            timer.cancel();
        }
        navigationService.resetNavigation();
        
        cancelRemoteNavigation();
        
        log.info("导航循环已停止，开始导航到充电桩");
        navigationService.startNavigation("充电桩");
    }
    
    private void cancelRemoteNavigation() {
        try {
            String cancelUrl = String.format("http://%s:%d/cmd/cancel_goal", configService.getNavServerHost(), configService.getNavServerPort());
            
            String response = HttpRequest.post(cancelUrl)
                    .header("Content-Type", "application/json")
                    .execute()
                    .body();
            
            log.info("调用停止导航接口成功，目标地址: {}, 响应: {}", cancelUrl, response);
        } catch (Exception e) {
            log.error("调用停止导航接口失败: {}", e.getMessage());
        }
    }

    @Override
    public String getCurrentDestination() {
        if (!running.get() || destinations == null || destinations.isEmpty()) {
            return null;
        }
        return destinations.get(currentIndex.get()).getDestination();
    }

    @Override
    public void pauseNavigation() {
        if (!running.get()) {
            log.warn("导航未运行，无法暂停");
            return;
        }
        if (paused.get()) {
            log.warn("导航已经处于暂停状态");
            return;
        }
        paused.set(true);
        if (timer != null) {
            timer.cancel();
        }
        log.info("导航已暂停");
    }

    @Override
    public void resumeNavigation() {
        if (!running.get()) {
            log.warn("导航未运行，无法继续");
            return;
        }
        if (!paused.get()) {
            log.warn("导航已经处于运行状态");
            return;
        }
        paused.set(false);
        startStatusCheckTimer();
        log.info("导航已继续");
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    @Override
    public void navigateToDestination(String destination) {
        log.info("开始独立导航到地点: {}", destination);
        
        this.wasRunningBeforeSingleNav = running.get();
        if (running.get()) {
            if (timer != null) {
                timer.cancel();
            }
            running.set(false);
        }
        
        this.singleDestination = destination;
        singleNavigation.set(true);
        
        navigationService.startNavigation(destination);
        
        startSingleNavigationStatusCheck();
    }

    private void startSingleNavigationStatusCheck() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!singleNavigation.get()) {
                    timer.cancel();
                    return;
                }

                NavStatus status = navigationService.getNavStatus();
                log.debug("独立导航状态: res={}, goal={}, dist={}, mileage={}", 
                        status.getRes(), status.getGoal(), status.getDist(), status.getMileage());

                if (status.getRes() == 3 && singleDestination.equals(status.getGoal())) {
                    timer.cancel();
                    handleSingleNavigationComplete(singleDestination);
                }
            }
        }, INITIAL_DELAY, STATUS_CHECK_INTERVAL);
    }

    private void handleSingleNavigationComplete(String goal) {
        log.info("独立导航完成，到达地点: {}", goal);
        
        pushAudioDrive(goal);
        
        singleNavigation.set(false);
        singleDestination = null;
        
        if (wasRunningBeforeSingleNav) {
            log.info("恢复之前的导航循环");
            running.set(true);
            startNextNavigation();
            wasRunningBeforeSingleNav = false;
        }
    }
}