package com.project.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.data.redis.config.ConfigureNotifyKeyspaceEventsAction;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

import com.project.service.TokenService;
import com.project.service.UserService;
//import com.project.repository.CustomSecurityContextRepository;
import com.project.util.MyFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "com.project.repository")
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	
	private final StringRedisTemplate redisTemplate;
	private final UserService userService;
	private final TokenService tokenService;
//	@Bean
//	public SecurityContextRepository securityContextRepository() {
//	    return new CustomSecurityContextRepository();
//	}
//	@Bean
//    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        return new StringRedisTemplate(redisConnectionFactory);
//    }

//	@Bean
//	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
//		RedisTemplate<String, String> template = new RedisTemplate<>();
//		template.setConnectionFactory(connectionFactory);
//		template.setKeySerializer(new StringRedisSerializer());
//		template.setValueSerializer(new StringRedisSerializer());
//		return template;
//	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring()
			.antMatchers("/google/**");
	}
//	@Bean
//	@ConditionalOnMissingBean // 해당 빈이 이미 등록되어 있지 않은 경우에만 등록됨
//	public ConfigureRedisAction configureRedisAction() {
//	    return ConfigureRedisAction.NO_OP;
//	}
	
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeRequests()
            .antMatchers("/**").permitAll()
            
            .and()
            .addFilterBefore(new MyFilter(redisTemplate, userService, tokenService), SecurityContextPersistenceFilter.class);
    
       		
//        	.csrf().disable()
//        	.authorizeRequests()
//        	.antMatchers("/google/**").permitAll()
//	 		.anyRequest().authenticated()
//	 		.and()
//	 		.addFilterBefore(new MyFilter(securityContextRepository), SecurityContextPersistenceFilter.class) // SecurityContext를 세션에 저장하기 위한 필터 추가
//	 		.sessionManagement()
//            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//            .maximumSessions(1);
        
	 		// 다른 구성 옵션들
//        	 	authorizeRequests
        
        	 		
    }
}