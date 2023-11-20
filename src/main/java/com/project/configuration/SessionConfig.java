package com.project.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

@Configuration
public class SessionConfig {

	@Bean
	public ConfigureRedisAction configureRedisAction() {
	   return ConfigureRedisAction.NO_OP;
	}
}

