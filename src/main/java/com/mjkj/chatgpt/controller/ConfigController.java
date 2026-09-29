package com.mjkj.chatgpt.controller;

import com.mjkj.chatgpt.model.ResultModel;
import com.mjkj.chatgpt.service.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;

    /**
     * 获取导航服务器配置
     */
    @GetMapping("/nav_server")
    public ResultModel getNavServerConfig() {
        ResultModel result = new ResultModel();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("host", configService.getNavServerHost());
            data.put("port", configService.getNavServerPort());
            data.put("navPath", configService.getNavServerNavPath());
            data.put("statusPath", configService.getNavServerStatusPath());
            result.setCode(200);
            result.setResultStr("查询成功");
            result.setData(data);
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 更新导航服务器配置
     */
    @PostMapping("/nav_server")
    public ResultModel updateNavServerConfig(@RequestBody Map<String, Object> request) {
        ResultModel result = new ResultModel();
        try {
            if (request.containsKey("host")) {
                String host = String.valueOf(request.get("host"));
                configService.setNavServerHost(host);
            }
            if (request.containsKey("port")) {
                int port = Integer.parseInt(String.valueOf(request.get("port")));
                configService.setNavServerPort(port);
            }
            if (request.containsKey("navPath")) {
                String navPath = String.valueOf(request.get("navPath"));
                configService.setNavServerNavPath(navPath);
            }
            if (request.containsKey("statusPath")) {
                String statusPath = String.valueOf(request.get("statusPath"));
                configService.setNavServerStatusPath(statusPath);
            }
            result.setCode(200);
            result.setResultStr("配置更新成功");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        return result;
    }

    /**
     * 清除导航服务器配置缓存（恢复默认值）
     */
    @DeleteMapping("/nav_server")
    public ResultModel clearNavServerConfig() {
        ResultModel result = new ResultModel();
        try {
            configService.clearNavServerConfig();
            result.setCode(200);
            result.setResultStr("配置缓存已清除，将使用默认值");
        } catch (Exception e) {
            result.setCode(500);
            result.setResultStr("系统错误：" + e.getMessage());
        }
        return result;
    }
}