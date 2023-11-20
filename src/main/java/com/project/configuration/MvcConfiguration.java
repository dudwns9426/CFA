package com.project.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//import com.project.interceptor.AccessTokenInterceptor;
import com.project.interceptor.LoggerInterceptor;
import com.project.service.TokenService;
import com.project.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {
	private final UserService userService;
	private final TokenService tokenService;
	
	
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoggerInterceptor()).excludePathPatterns("/css/**", "/fonts/**", "/plugin/**",
        "/scripts/**");
    
//    registry.addInterceptor(new AccessTokenInterceptor(userService, tokenService)).excludePathPatterns("/google/login/**");
    
  }

}