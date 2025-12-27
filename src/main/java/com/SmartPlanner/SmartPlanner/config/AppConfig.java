package com.SmartPlanner.SmartPlanner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * APP CONFIG - Application beans configure karta hai
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate bean - External APIs call karne ke liye
     * (Open-Meteo Weather API)
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
