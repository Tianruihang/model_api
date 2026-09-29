package com.mjkj.chatgpt.service.impl;

import com.mjkj.chatgpt.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${navigation.server.host:192.168.1.3}")
    private String defaultHost;

    @Value("${navigation.server.port:80}")
    private int defaultPort;

    @Value("${navigation.server.nav-path:/cmd/nav_name}")
    private String defaultNavPath;

    @Value("${navigation.server.status-path:/reeman/nav_status}")
    private String defaultStatusPath;

    private static final String REDIS_KEY_HOST = "navigation:server:host";
    private static final String REDIS_KEY_PORT = "navigation:server:port";
    private static final String REDIS_KEY_NAV_PATH = "navigation:server:nav-path";
    private static final String REDIS_KEY_STATUS_PATH = "navigation:server:status-path";

    @Override
    public String getNavServerHost() {
        String value = getFromCache(REDIS_KEY_HOST);
        if (value != null) {
            return value;
        }
        log.debug("缓存中未找到导航服务器host，使用默认值: {}", defaultHost);
        return defaultHost;
    }

    @Override
    public void setNavServerHost(String host) {
        setToCache(REDIS_KEY_HOST, host);
        log.info("更新导航服务器host为: {}", host);
    }

    @Override
    public int getNavServerPort() {
        String value = getFromCache(REDIS_KEY_PORT);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("缓存中的端口值无效: {}，使用默认值", value);
            }
        }
        log.debug("缓存中未找到导航服务器port，使用默认值: {}", defaultPort);
        return defaultPort;
    }

    @Override
    public void setNavServerPort(int port) {
        setToCache(REDIS_KEY_PORT, String.valueOf(port));
        log.info("更新导航服务器port为: {}", port);
    }

    @Override
    public String getNavServerNavPath() {
        String value = getFromCache(REDIS_KEY_NAV_PATH);
        if (value != null) {
            return value;
        }
        log.debug("缓存中未找到导航服务器nav-path，使用默认值: {}", defaultNavPath);
        return defaultNavPath;
    }

    @Override
    public void setNavServerNavPath(String navPath) {
        setToCache(REDIS_KEY_NAV_PATH, navPath);
        log.info("更新导航服务器nav-path为: {}", navPath);
    }

    @Override
    public String getNavServerStatusPath() {
        String value = getFromCache(REDIS_KEY_STATUS_PATH);
        if (value != null) {
            return value;
        }
        log.debug("缓存中未找到导航服务器status-path，使用默认值: {}", defaultStatusPath);
        return defaultStatusPath;
    }

    @Override
    public void setNavServerStatusPath(String statusPath) {
        setToCache(REDIS_KEY_STATUS_PATH, statusPath);
        log.info("更新导航服务器status-path为: {}", statusPath);
    }

    @Override
    public void clearNavServerConfig() {
        redisTemplate.delete(REDIS_KEY_HOST);
        redisTemplate.delete(REDIS_KEY_PORT);
        redisTemplate.delete(REDIS_KEY_NAV_PATH);
        redisTemplate.delete(REDIS_KEY_STATUS_PATH);
        log.info("已清除导航服务器配置缓存");
    }

    private String getFromCache(String key) {
        try {
            if (redisTemplate != null && redisTemplate.hasKey(key)) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null && !value.isEmpty()) {
                    log.debug("从缓存获取配置成功，key={}, value={}", key, value);
                    return value;
                }
            }
        } catch (Exception e) {
            log.warn("从缓存获取配置失败: {}", e.getMessage());
        }
        return null;
    }

    private void setToCache(String key, String value) {
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(key, value);
                log.debug("配置已存入缓存，key={}, value={}", key, value);
            }
        } catch (Exception e) {
            log.error("存入缓存失败: {}", e.getMessage());
        }
    }
}