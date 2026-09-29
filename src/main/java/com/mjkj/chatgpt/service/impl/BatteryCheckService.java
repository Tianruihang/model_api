package com.mjkj.chatgpt.service.impl;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import com.mjkj.chatgpt.model.BatteryStatus;
import com.mjkj.chatgpt.service.NavigationManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class BatteryCheckService {
    private final NavigationManagerService navigationManagerService;
    
    @Value("${navigation.server.host:192.168.1.8}")
    private String navServerHost;
    
    @Value("${navigation.server.port:80}")
    private int navServerPort;
    
    private static final String BATTERY_API_PATH = "/reeman/base_encode";
    
    private static final int LOW_BATTERY_THRESHOLD = 5;
    private static final String CHARGING_STATION = "充电桩";
    
    private volatile boolean alreadyNavigatedToCharging = false;
    
    public BatteryCheckService(NavigationManagerService navigationManagerService) {
        this.navigationManagerService = navigationManagerService;
    }
    
    @PostConstruct
    public void init() {
        log.info("电池检查服务已启动，每隔30分钟检查一次电量");
    }
    
    public void checkBatteryStatus() {
        try {
            BatteryStatus status = fetchBatteryStatus();
            if (status == null) {
                return;
            }
            
            log.info("电池状态检查 - 电量: {}%, 充电状态: {}, 急停按钮: {}", 
                    status.getBattery(), 
                    getChargeFlagDescription(status.getChargeFlag()),
                    status.isEmergencyPressed() ? "按下" : "弹起");
            
            if (status.isLowBattery() && !status.isCharging()) {
                handleLowBattery();
            } else if (!status.isLowBattery()) {
                alreadyNavigatedToCharging = false;
            }
        } catch (Exception e) {
            log.error("电池状态检查异常: {}", e.getMessage());
        }
    }
    
    private BatteryStatus fetchBatteryStatus() {
        try {
            String statusUrl = String.format("http://%s:%d%s", navServerHost, navServerPort, BATTERY_API_PATH);
            String response = HttpRequest.get(statusUrl)
                    .header("Content-Type", "application/json")
                    .execute()
                    .body();
            
            log.debug("获取电池状态成功，目标地址: {}, 响应: {}", statusUrl, response);
            
            JSONObject jsonResponse = JSONObject.parseObject(response);
            if (jsonResponse != null) {
                return new BatteryStatus(
                        jsonResponse.getIntValue("battery"),
                        jsonResponse.getIntValue("chargeFlag"),
                        jsonResponse.getIntValue("emergencyButton")
                );
            }
        } catch (Exception e) {
            log.warn("获取电池状态失败: {}", e.getMessage());
        }
        return null;
    }
    
    private void handleLowBattery() {
        if (alreadyNavigatedToCharging) {
            log.info("已执行过低电量处理，跳过本次");
            return;
        }
        
        log.warn("电量低于{}%且未充电，停止所有导航并前往充电桩", LOW_BATTERY_THRESHOLD);
        
        String currentDestination = navigationManagerService.getCurrentDestination();
        if (currentDestination != null && !currentDestination.equals(CHARGING_STATION)) {
            navigationManagerService.stopNavigationLoop();
            alreadyNavigatedToCharging = true;
        }
    }
    
    private String getChargeFlagDescription(int chargeFlag) {
        switch (chargeFlag) {
            case 2:
                return "充电桩充电中";
            case 3:
                return "适配器充电中";
            case 8:
                return "正在对接充电桩";
            default:
                return "非充电状态(" + chargeFlag + ")";
        }
    }
    
    public void resetNavigationFlag() {
        alreadyNavigatedToCharging = false;
    }
}