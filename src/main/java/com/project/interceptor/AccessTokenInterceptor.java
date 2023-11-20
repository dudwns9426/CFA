package com.project.interceptor;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.dto.AccessTokenInfo;
import com.project.domain.dto.RefreshDTO;
import com.project.domain.entity.Token;
import com.project.service.TokenService;
import com.project.service.UserService;
import com.project.util.ExceptionUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccessTokenInterceptor extends HandlerInterceptorAdapter {

	private final UserService userService;
	private final TokenService tokenService;
	private String sessionId;

	@Override
	public boolean preHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object handler)
			throws Exception {
		// 세션id 추출
		System.out.println("=====preHandle start=====");
		sessionId = userService.extractSessionId(httpRequest);
		// 액세스 토큰 추출
		String accessToken = userService.extractAccessToken(httpRequest);

		// 추출한 액세스토큰과 세션id가 null이 아니라면
		if (accessToken != null && sessionId != null) {
			System.out.println("sessionId: " + sessionId);
			System.out.println("RequestAccessToken: " + accessToken);
			// redis에 저장한 액세스토큰 호출
			AccessTokenInfo redisAccessTokenInfo = userService.getAccessTokenInfoFromRedis(sessionId);

			// redis에서 가져온 값이 비어있지 않다면
			if (redisAccessTokenInfo != null) {
				String redisAccessToken = redisAccessTokenInfo.getAccessToken();
				System.out.println("RedisAccessToken: " + redisAccessToken);
				// 액세스 토큰이 일치한다면
				if (accessToken.equals(redisAccessToken)) {
					System.out.println("Success!");
					try {
						return true;
					} catch (Exception e) {
						throw e;
					}
				} else {// 액세스 토큰이 일치하지 않는다면(재로그인 요구)
					httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return false;
				}
			} else {// redis토큰에서 가져온 값 비어있다면(만료일자 지났거나 서버문제, 재로그인 요구)
				httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return false;
			}
		} else {// 추출한 액세스토큰 또는 세션id가 null이라면(refreshToken의 요청 일 수 있음, 재로그인 요구)
			// 사용자 구분이 된다면
			if (sessionId != null) {
				String refreshToken = userService.extractRefreshToken(httpRequest);
				// 추출한 리프레시토큰이 null이 아니면
				if (refreshToken != null) {
					Token token = tokenService.findBySessionId(sessionId);
					// 세션아이디와 매칭되는 토큰이 있다면
					if (token != null) {
						String dbRefreshToken = token.getRefreshToken();
						// 추출한 리프레시토큰과 DB리프레시토큰이 일치한다면
						if (dbRefreshToken.equals(refreshToken)) {
							// 만료시간 검증 됐다면
							if (tokenService.isRefreshTokenExpired(token)) {
								return true;
								// 만료시간이 지났다면
							} else {// 재로그인 요구
								return false;
							}
							// 추출한 리프레시토큰과 DB리프레시토큰이 일치하지 않는다면
						} else {// 재로그인 요구
							return false;
						}
						// 세션아이디와 매칭되는 토큰이 없다면
					} else {
						return false;
					}
					// 추출한 리프레시토큰이 null이면
				} else {
					return false;
				}
				// 사용자 구분이 되지 않는다면
			} else {
				return false;
			}
		}
	}

	@Override
	public void postHandle(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Object handler,
			ModelAndView modelAndView) {
		// 컨트롤러 실행 후에 실행되는 로직
		System.out.println("=====postHandle start=====");

		if (httpResponse.getStatus() == 200) { // 성공적인 응답에 대해서
			// response body에 accessToken 추가.
			sessionId = userService.extractSessionId(httpRequest);
			if (sessionId != null) {
				Token token = tokenService.findBySessionId(sessionId);
				if (token != null) {
					String refreshToken = token.getRefreshToken();
					// refreshToken으로 accessToken 재발급
					ResponseEntity<String> refreshAccessTokenInfo = tokenService.refreshAccessToken(refreshToken);
					if (refreshAccessTokenInfo != null) {
						String responseBody = refreshAccessTokenInfo.getBody(); // HTTP 응답의 본문 가져오기
						// 역직렬화
						ObjectMapper objectMapper = new ObjectMapper();
						try {
							RefreshDTO refreshDTO = objectMapper.readValue(responseBody, RefreshDTO.class);
							String accessToken = refreshDTO.getAccessToken();
							long expiresIn = Long.parseLong(refreshDTO.getExpiresIn());
							// redis에 저장
							userService.setRedis(accessToken, expiresIn, sessionId);
							System.out.println("헤더 추가 전");
							httpResponse.addHeader("Authorization", "Bearer " + accessToken);
						} catch (IOException e) {
							System.out.println("캐치" + e.toString());
							throw new ExceptionUtil(e);
						}
					}
				}
			}
		}
	}
	
}