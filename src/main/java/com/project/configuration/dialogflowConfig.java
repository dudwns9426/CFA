package com.project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class dialogflowConfig {
    
    @Bean
    public Resource myResource() {
        return new ClassPathResource("raw/chatbot-401706-fe837d7d480d.json");
    }
}