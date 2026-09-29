package com.mjkj.chatgpt.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mjkj.chatgpt.model.NavStatus;
import com.mjkj.chatgpt.service.ConfigService;
import com.mjkj.chatgpt.service.NavigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class NavigationServiceImpl implements NavigationService {
    private final AtomicBoolean navigating = new AtomicBoolean(false);
    private final AtomicReference<String> currentGoal = new AtomicReference<>("");
    private final AtomicReference<NavStatus> navStatus = new AtomicReference<>();
    private long startTime = 0;

    @Autowired
    private ConfigService configService;

    @Override
    public void startNavigation(String point) {
        navigateToPoint(point);
        navigating.set(true);
        currentGoal.set(point);
        startTime = System.currentTimeMillis();
        navStatus.set(new NavStatus(1, 0, point, 1.0, 0));
        log.info("开始导航到位置: {}", point);
    }
    
    private void navigateToPoint(String point) {
        try {
            String navApiUrl = String.format("http://%s:%d%s", configService.getNavServerHost(), configService.getNavServerPort(), configService.getNavServerNavPath());
            JSONObject jsonMsg = new JSONObject();
            jsonMsg.put("point", point);
            
            String response = HttpRequest.post(navApiUrl)
                    .header("Content-Type", "application/json")
                    .body(jsonMsg.toJSONString())
                    .execute()
                    .body();
            
            log.info("发送导航请求成功，目标地址: {}, 响应: {}", navApiUrl, response);
        } catch (Exception e) {
            log.error("发送导航请求失败: {}", e.getMessage());
        }
    }

    @Override
    public NavStatus getNavStatus() {
        NavStatus remoteStatus = fetchRemoteNavStatus();
        if (remoteStatus != null) {
            return remoteStatus;
        }

        if (!navigating.get()) {
            return new NavStatus(6, 0, "", 0, 0);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        
        if (elapsed >= 10000) {
            navStatus.set(new NavStatus(3, 0, currentGoal.get(), 0, 0.6));
            navigating.set(false);
            log.info("导航完成，到达位置: {}", currentGoal.get());
        } else if (elapsed >= 5000) {
            navStatus.set(new NavStatus(1, 0, currentGoal.get(), 0.5, 0));
        }

        return navStatus.get();
    }
    
    private NavStatus fetchRemoteNavStatus() {
        try {
            String statusUrl = String.format("http://%s:%d%s", configService.getNavServerHost(), configService.getNavServerPort(), configService.getNavServerStatusPath());
            String response = HttpRequest.get(statusUrl)
                    .header("Content-Type", "application/json")
                    .execute()
                    .body();
            
            log.info("获取导航状态成功，目标地址: {}, 响应: {}", statusUrl, response);
            
            JSONObject jsonResponse = JSONObject.parseObject(response);
            if (jsonResponse != null) {
                int res = jsonResponse.getIntValue("res");
                String localGoal = currentGoal.get();
                
                if (localGoal == null || localGoal.isEmpty()) {
                    localGoal = jsonResponse.getString("goal");
                }
                
                NavStatus status = new NavStatus(
                        res,
                        jsonResponse.getIntValue("reason"),
                        localGoal,
                        jsonResponse.getDoubleValue("dist"),
                        jsonResponse.getDoubleValue("mileage")
                );
                
                syncLocalStatus(status);
                
                return status;
            }
        } catch (Exception e) {
            log.warn("获取远程导航状态失败，使用本地模拟状态: {}", e.getMessage());
        }
        return null;
    }
    
    private void syncLocalStatus(NavStatus remoteStatus) {
        if (remoteStatus == null) {
            return;
        }
        
        int res = remoteStatus.getRes();
        if (res == 3) {
            navigating.set(false);
            currentGoal.set(remoteStatus.getGoal());
            log.info("远程导航完成，到达位置: {}", remoteStatus.getGoal());
        } else if (res == 1) {
            navigating.set(true);
            currentGoal.set(remoteStatus.getGoal());
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
        } else if (res == 6) {
            navigating.set(false);
            currentGoal.set("");
            startTime = 0;
        }
    }

    @Override
    public boolean isNavigating() {
        return navigating.get();
    }

    @Override
    public void resetNavigation() {
        navigating.set(false);
        currentGoal.set("");
        navStatus.set(null);
        startTime = 0;
    }
}