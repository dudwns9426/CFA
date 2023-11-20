package com.project.service;

import java.time.Instant;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.project.domain.dto.TokenResponse;
import com.project.domain.entity.Token;
import com.project.domain.entity.User;
import com.project.repository.TokenRepository;
import com.project.util.ConfigUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenService {
	private final TokenRepository tokenRepository;
	private final ConfigUtils configUtils;

	public void saveToken(Long userId, TokenResponse googleLoginResponse, String sessionId) {
		// Token 엔터티 생성
		String refreshToken = googleLoginResponse.getRefreshToken();
		Token token = new Token();
		token.setRefreshToken(refreshToken);

		// User 엔터티 생성 (또는 이미 있는 User 엔터티를 가져와도 됨)
		User user = new User();
		user.setUser_id(userId); // userId는 User 엔터티의 기본키 (user_id) 값

		// Token 엔터티에 User를 설정
		token.setUser(user);
		token.setSessionId(sessionId);

		// Token 엔터티를 저장
		tokenRepository.save(token);
	}

	@Transactional
	public void updateRefreshToken(Long userId, String refreshToken, String sessionId) {
		Instant currentDateTime = Instant.now();
	    Instant expirationTime = currentDateTime.plusSeconds(180 * 24 * 60 * 60); // 180일을 더한 만료 시간

	    tokenRepository.updateRefreshToken(userId, refreshToken, currentDateTime, expirationTime, sessionId);
	}
	
	@Transactional
	public void updateSessionId(Long userId, String sessionId) {
		Instant currentDateTime = Instant.now();
		tokenRepository.updateSessionId(userId, currentDateTime, sessionId);
	}
	
	public Token findBySessionId(String sessionId) {
		if(sessionId != null) {
			return tokenRepository.findBySessionId(sessionId);
		} else {
			return null;
		}
		
	}
	
	public boolean isRefreshTokenExpired(Token token) {

	    if (token != null) {
	    	Instant expirationTime = token.getExpirationTime();
	    	Instant currentDateTime = Instant.now();

	        // 현재 시간과 만료 시간 비교
	        return currentDateTime.isAfter(expirationTime);
	    } else {
	    	// 토큰이 없으면 재로그인 요구
	    	return false;
	    }
	}
	
	public ResponseEntity<String> refreshAccessToken(String refreshToken) {
        RestTemplate restTemplate = new RestTemplate();

        // Set the request parameters
        MultiValueMap<String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("grant_type", "refresh_token");
        requestParams.add("refresh_token", refreshToken);
        requestParams.add("client_id", configUtils.getGoogleClientId());
        requestParams.add("client_secret", configUtils.getGoogleSecret());

        // Set the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create the request
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestParams, headers);

        // Send the request
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token",
                requestEntity,
                String.class
        );
        System.out.println(responseEntity);
        return responseEntity;
    }

}
