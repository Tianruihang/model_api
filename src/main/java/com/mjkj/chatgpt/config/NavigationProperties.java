package com.mjkj.chatgpt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "custom")
public class NavigationProperties {
    
    private Map<String, String> destinations = new LinkedHashMap<>();
    
    public List<NavigationItem> getNavigationItems() {
        if (destinations == null || destinations.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return destinations.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    String[] parts = value.split(",");
                    String destination = parts[0].trim();
                    long delayMinutes = parts.length > 1 ? Long.parseLong(parts[1].trim()) : 5;
                    String audioPath = parts.length > 2 ? parts[2].trim() : null;
                    return new NavigationItem(Integer.parseInt(key), destination, delayMinutes, audioPath);
                })
                .sorted((a, b) -> Integer.compare(a.getKey(), b.getKey()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Data
    public static class NavigationItem {
        private int key;
        private String destination;
        private long delayMinutes;
        private String audioPath;
        
        public NavigationItem(int key, String destination, long delayMinutes) {
            this.key = key;
            this.destination = destination;
            this.delayMinutes = delayMinutes;
        }
        
        public NavigationItem(int key, String destination, long delayMinutes, String audioPath) {
            this.key = key;
            this.destination = destination;
            this.delayMinutes = delayMinutes;
            this.audioPath = audioPath;
        }
        
        public long getDelayMillis() {
            return delayMinutes * 60 * 1000;
        }
    }
}