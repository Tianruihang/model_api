package com.mjkj.chatgpt.service;

import java.util.List;

public interface NavigationManagerService {
    void startNavigationLoop(List<String> destinations);
    void stopNavigationLoop();
    String getCurrentDestination();
    void pauseNavigation();
    void resumeNavigation();
    boolean isPaused();
    void navigateToDestination(String destination);
}