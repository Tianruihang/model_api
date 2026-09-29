package com.mjkj.chatgpt.service;

public interface ConfigService {
    String getNavServerHost();
    void setNavServerHost(String host);
    int getNavServerPort();
    void setNavServerPort(int port);
    String getNavServerNavPath();
    void setNavServerNavPath(String navPath);
    String getNavServerStatusPath();
    void setNavServerStatusPath(String statusPath);
    void clearNavServerConfig();
}