package com.mjkj.chatgpt;

import com.mjkj.chatgpt.config.NavigationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NavigationProperties.class)
public class ChatGptApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatGptApplication.class, args);
        System.out.println("*************** ChatGPT 启动成功 *******************");
    }

}
