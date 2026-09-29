package com.mjkj.chatgpt.service;

import com.mjkj.chatgpt.model.NavStatus;

public interface NavigationService {
    void startNavigation(String point);
    NavStatus getNavStatus();
    boolean isNavigating();
    void resetNavigation();
}