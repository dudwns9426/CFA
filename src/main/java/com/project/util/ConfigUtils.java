package com.project.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 설정 정보를 관리하는 유틸리티 클래스입니다. 주로 Google OAuth2 관련 설정 정보를 제공합니다.
 * 
 * @author Jeon Youngjun
 */
@Component
public class ConfigUtils {
	@Value("${google.auth.url}")
	private String googleAuthUrl;

	@Value("${google.login.url}")
	private String googleLoginUrl;

	@Value("${google.redirect.uri}")
	private String googleRedirectUrl;

	@Value("${google.client.id}")
	private String googleClientId;

	@Value("${google.secret}")
	private String googleSecret;

	@Value("${google.auth.scope}")
	private String scopes;

	/**
	 * Google 로그인 URL을 초기화하고 반환합니다.
	 * 
	 * @return Google 로그인 URL
	 */
	public String googleInitUrl() {
		Map<String, Object> params = new HashMap<>();
		params.put("client_id", getGoogleClientId());
		params.put("redirect_uri", getGoogleRedirectUri());
		params.put("response_type", "code");
		params.put("scope", getScopeUrl());
		params.put("access_type", "offline");
		params.put("prompt", "consent");

		String paramStr = params.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue())
				.collect(Collectors.joining("&"));

		return getGoogleLoginUrl() + "/o/oauth2/v2/auth" + "?" + paramStr;
	}

	public String getGoogleAuthUrl() {
		return googleAuthUrl;
	}

	public String getGoogleLoginUrl() {
		return googleLoginUrl;
	}

	public String getGoogleClientId() {
		return googleClientId;
	}

	public String getGoogleRedirectUri() {
		return googleRedirectUrl;
	}

	public String getGoogleSecret() {
		return googleSecret;
	}

	/**
	 * OAuth2 Scope의 값을 반환합니다. 띄어쓰기를 UTF-8로 변환하여 반환합니다.
	 * 
	 * @return OAuth2 Scope 값
	 */
	public String getScopeUrl() {
		return scopes.replaceAll(",", "%20");
	}
}