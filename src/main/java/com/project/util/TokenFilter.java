package com.project.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

import com.project.domain.entity.Token;
import com.project.domain.dto.RefreshDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.dto.AccessTokenInfo;
import com.project.service.TokenService;
import com.project.service.UserService;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 사용자의 요청을 필터링하여 액세스 토큰 및 세션 ID를 처리하는 필터 클래스입니다.
 * 주로 Redis를 통해 토큰을 관리하고, 액세스 토큰을 검증하여 사용자의 요청을 처리합니다.
 * 
 * @author Jeon Youngjun
 */
public class TokenFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final UserService userService;
    private final TokenService tokenService;

    /**
     * TokenFilter의 생성자입니다.
     * 
     * @param redisTemplate Redis를 사용하기 위한 StringRedisTemplate
     * @param userService 사용자 정보를 처리하는 UserService
     * @param tokenService 토큰 관리를 담당하는 TokenService
     */
    public TokenFilter(StringRedisTemplate redisTemplate, UserService userService, TokenService tokenService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    /**
     * 실제 필터링 작업을 수행하는 메서드입니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 세션 ID 및 액세스 토큰 추출
        String sessionId = userService.extractSessionId(request);
        String accessToken = userService.extractAccessToken(request);

        // 액세스 토큰 및 세션 ID가 존재하는 경우
        if (accessToken != null && sessionId != null) {
        	System.out.println("RequestAceessToken:"+accessToken);
            processValidAccessToken(request, response, chain, sessionId, accessToken);
        } else {
            // 액세스 토큰 또는 세션 ID가 존재하지 않는 경우
            handleNullAccessTokenOrSessionId(request, response, chain, sessionId);
        }
    }

    /**
     * 액세스 토큰이 유효한 경우 처리 로직을 수행합니다.
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param chain FilterChain
     * @param sessionId 세션 ID
     * @param accessToken 액세스 토큰
     */
    private void processValidAccessToken(HttpServletRequest request, HttpServletResponse response, 
    									FilterChain chain, String sessionId, String accessToken) throws IOException, ServletException {
        // Redis에서 액세스 토큰 정보 가져오기
        AccessTokenInfo redisAccessTokenInfo = userService.getAccessTokenInfoFromRedis(sessionId);

        if (redisAccessTokenInfo != null) {
            String redisAccessToken = redisAccessTokenInfo.getAccessToken();
            System.out.println("RedisAccessToken:" + redisAccessToken);
            if (accessToken.equals(redisAccessToken)) {
                // 커스텀 인증 설정 및 필터 체이닝 계속
                setCustomAuthenticationAndContinueFiltering(request, response, chain, sessionId);
            } else {
                response.setStatus(455);
                return;
            }
        } else {
            response.setStatus(456);
            return;
        }
    }

    /**
     * 커스텀 인증 설정 및 필터 체이닝을 계속합니다.
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param chain FilterChain
     * @param sessionId 세션 ID
     */
    private void setCustomAuthenticationAndContinueFiltering(HttpServletRequest request, HttpServletResponse response, 
    														FilterChain chain, String sessionId) 
    														throws IOException, ServletException {
//        CustomAuthentication auth = new CustomAuthentication(sessionId, accessToken);
//        SecurityContextHolder.getContext().setAuthentication(auth);

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, responseWrapper);

        if (response.getStatus() == 200) {
            handleSuccessfulResponse(sessionId, request, response, responseWrapper);
        } else {
            responseWrapper.copyBodyToResponse();
        }
    }

    /**
     * 성공적인 응답을 처리합니다.
     * 
     * @param sessionId 세션 ID
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param responseWrapper ContentCachingResponseWrapper
     */
    private void handleSuccessfulResponse(String sessionId, HttpServletRequest request, 
    									  HttpServletResponse response, ContentCachingResponseWrapper responseWrapper) 
    									  throws IOException {
    	
        sessionId = userService.extractSessionId(request);
        if (sessionId != null) {
            Token token = tokenService.findBySessionId(sessionId);
            if (token != null) {
                String refreshToken = token.getRefreshToken();
                ResponseEntity<String> refreshAccessTokenInfo = tokenService.refreshAccessToken(refreshToken);
                if (refreshAccessTokenInfo != null) {
                    handleRefreshAccessToken(request, response, responseWrapper, refreshAccessTokenInfo, sessionId);
                }
            }
        }
    }

    /**
     * 액세스 토큰을 재발급하는 로직을 처리합니다.
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param responseWrapper ContentCachingResponseWrapper
     * @param refreshAccessTokenInfo 액세스 토큰 재발급 정보
     * @param sessionId 세션 ID
     */
    private void handleRefreshAccessToken(HttpServletRequest request, HttpServletResponse response, 
    									  ContentCachingResponseWrapper responseWrapper, 
    									  ResponseEntity<String> refreshAccessTokenInfo, String sessionId) throws IOException {
    	
        String responseBody = refreshAccessTokenInfo.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        RefreshDTO refreshDTO = objectMapper.readValue(responseBody, RefreshDTO.class);
        String accessToken = refreshDTO.getAccessToken();
        long expiresIn = Long.parseLong(refreshDTO.getExpiresIn());

        userService.setRedis(accessToken, expiresIn, sessionId);
        System.out.println("RedisUpdate");
        responseWrapper.setHeader("Authorization", "Bearer " + accessToken);
        System.out.println("HeaderUpdate");
        responseWrapper.copyBodyToResponse();
    }

    /**
     * 액세스 토큰 또는 세션 ID가 null인 경우의 처리 로직입니다.
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param chain FilterChain
     * @param sessionId 세션 ID
     */
    private void handleNullAccessTokenOrSessionId(HttpServletRequest request, HttpServletResponse response, 
    											 FilterChain chain, String sessionId) throws IOException {
       
        try {
            if (sessionId != null) {
                String refreshToken = userService.extractRefreshToken(request);
                if (refreshToken != null) {
                    Token token = tokenService.findBySessionId(sessionId);
                    if (token != null) {
                        String dbRefreshToken = token.getRefreshToken();
                        if (dbRefreshToken.equals(refreshToken)) {
                            if (tokenService.isRefreshTokenExpired(token)) {
                                // RefreshToken이 만료되지 않았을 때
                            	setCustomAuthenticationAndContinueFiltering(request, response, chain, sessionId);
                            } else {
                                // RefreshToken이 만료되었을 때 - 재로그인 요구
                                response.setStatus(450);
                                return;
                            }
                        } else {
                            // RefreshToken이 일치하지 않을 때 - 재로그인 요구
                            response.setStatus(451);
                            return;
                        }
                    } else {
                        // 세션아이디에 해당하는 토큰이 없을 때 - 재로그인 요구
                        response.setStatus(452);
                        return;
                    }
                } else {
                    // 추출된 RefreshToken이 null일 때 - 재로그인 요구
                    response.setStatus(453);
                    return;
                }
            } else {
                // 세션 ID가 null일 때 - 재로그인 요구
                response.setStatus(454);
                return;
            }
        } catch (IOException | ServletException e) {
            throw new ExceptionUtil(e);
        }
    }
}
