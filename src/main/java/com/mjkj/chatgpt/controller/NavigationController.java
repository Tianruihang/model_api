package com.mjkj.chatgpt.controller;

import com.mjkj.chatgpt.model.NavRequest;
import com.mjkj.chatgpt.model.NavStatus;
import com.mjkj.chatgpt.model.ResultModel;
import com.mjkj.chatgpt.service.NavigationManagerService;
import com.mjkj.chatgpt.service.NavigationService;
import com.mjkj.chatgpt.service.impl.NavigationManagerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 移动数字人导航控制器
 */
@RestController
@CrossOrigin(origins = "*")
public class NavigationController {
    
    @Autowired
    private NavigationService navigationService;
    
    @Autowired
    private NavigationManagerService navigationManagerService;
    
    /**
     * 导航到指定位置
     */
    @PostMapping("/cmd/nav_name")
    public ResultModel navigateToPoint(@RequestBody NavRequest request) {
        ResultModel result = new ResultModel();
        
        try {
            navigationService.startNavigation(request.getPoint());
            result.setCode(200);
            result.setResultStr("开始导航");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取导航状态
     */
    @GetMapping("/reeman/nav_status")
    public ResultModel getNavigationStatus() {
        ResultModel result = new ResultModel();
        
        try {
            NavStatus status = navigationService.getNavStatus();
            result.setCode(200);
            result.setResultStr("查询成功");
            result.setData(status);
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 启动导航循环
     */
    @PostMapping("/nav/start_loop")
    public ResultModel startNavigationLoop(@RequestBody List<String> destinations) {
        ResultModel result = new ResultModel();
        
        try {
            navigationManagerService.startNavigationLoop(destinations);
            result.setCode(200);
            result.setResultStr("导航循环已启动");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 启动默认导航循环
     */
    @PostMapping("/nav/start_loop_default")
    public ResultModel startDefaultNavigationLoop() {
        ResultModel result = new ResultModel();
        
        try {
            List<String> defaultDestinations = Arrays.asList("A", "B", "C", "D", "E");
            navigationManagerService.startNavigationLoop(defaultDestinations);
            result.setCode(200);
            result.setResultStr("默认导航循环已启动，地点: A -> B -> C -> D -> E");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 停止导航循环
     */
    @PostMapping("/nav/stop_loop")
    public ResultModel stopNavigationLoop() {
        ResultModel result = new ResultModel();
        
        try {
            navigationManagerService.stopNavigationLoop();
            result.setCode(200);
            result.setResultStr("导航循环已停止");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取当前导航地点
     */
    @GetMapping("/nav/current")
    public ResultModel getCurrentDestination() {
        ResultModel result = new ResultModel();
        
        try {
            String destination = navigationManagerService.getCurrentDestination();
            if (destination != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("currentDestination", destination);
                data.put("isPaused", navigationManagerService.isPaused());
                result.setCode(200);
                result.setResultStr("查询成功");
                result.setData(data);
            } else {
                result.setCode(404);
                result.setResultStr("未在导航中");
            }
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 暂停导航
     */
    @PostMapping("/nav/pause")
    public ResultModel pauseNavigation() {
        ResultModel result = new ResultModel();
        
        try {
            navigationManagerService.pauseNavigation();
            result.setCode(200);
            result.setResultStr("导航已暂停");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 继续导航
     */
    @PostMapping("/nav/resume")
    public ResultModel resumeNavigation() {
        ResultModel result = new ResultModel();
        
        try {
            navigationManagerService.resumeNavigation();
            result.setCode(200);
            result.setResultStr("导航已继续");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 从配置文件第一个地点开始导航（触发后从1开始导航）
     */
    @PostMapping("/nav/start_from_config")
    public ResultModel startNavigationFromConfig() {
        ResultModel result = new ResultModel();
        
        try {
            ((NavigationManagerServiceImpl) navigationManagerService).startNavigationLoopFromConfig();
            result.setCode(200);
            result.setResultStr("从配置文件第一个地点开始导航");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 导航到指定地点（独立导航，到达后自动播报音频）
     */
    @PostMapping("/nav/go_to")
    public ResultModel navigateToSpecificPoint(@RequestBody Map<String, String> request) {
        ResultModel result = new ResultModel();
        
        try {
            String destination = request.get("destination");
            if (destination == null || destination.trim().isEmpty()) {
                result.setCode(400);
                result.setResultStr("参数错误：destination不能为空");
                return result;
            }
            
            navigationManagerService.navigateToDestination(destination);
            result.setCode(200);
            result.setResultStr("开始导航到：" + destination);
            
            Map<String, Object> data = new HashMap<>();
            data.put("destination", destination);
            result.setData(data);
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        
        return result;
    }
}