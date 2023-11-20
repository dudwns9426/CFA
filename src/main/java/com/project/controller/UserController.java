package com.project.controller;

import java.time.Instant;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.repository.CustomSecurityContextRepository;
import com.project.domain.dto.GoogleLoginRequest;
import com.project.domain.dto.IdTokenDTO;
import com.project.domain.dto.TokenResponse;
import com.project.domain.dto.response.FirstLoginResponse;
import com.project.domain.dto.response.LoginResponse;
import com.project.domain.entity.Token;
import com.project.domain.entity.User;
import com.project.repository.TokenRepository;
import com.project.service.TokenService;
import com.project.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/google")
public class UserController {

	@Autowired
	private TokenRepository tokenRepository;

//    private final ConfigUtils configUtils;
	private final UserService userService;
	private final TokenService tokenService;
//    private final CustomSecurityContextRepository securityContextRepository;

//    @PostMapping(value = "/login")
//    public ResponseEntity<Object> moveGoogleInitUrl() {
//        String authUrl = configUtils.googleInitUrl();
//        URI redirectUri = null;
//        try {
//            redirectUri = new URI(authUrl);
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.setLocation(redirectUri);
//            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//
//        return ResponseEntity.badRequest().build();
//    }

	@PostMapping(value = "/login/redirect")
	public ResponseEntity<?> redirectGoogleLogin(@RequestParam(value = "code") String authCode,
			HttpServletRequest httpServletRequest) {
		System.out.println("code:" + authCode);

		GoogleLoginRequest requestParams = userService.makeRequest(authCode);

		System.out.println(requestParams);

		try {
			ResponseEntity<String> apiResponseJson = userService.apiResponseJson(requestParams);
			System.out.println("apiResponseJson:" + apiResponseJson);

			TokenResponse googleLoginResponse = userService.googleLoginResponse(apiResponseJson);
			System.out.println("TokenResponse:" + googleLoginResponse);

			String resultJson = userService.resultJson(googleLoginResponse);
			System.out.println("resultJson:" + resultJson);
			if (resultJson != null) {
				ObjectMapper objectMapper = new ObjectMapper();
				IdTokenDTO idTokenDTO = objectMapper.readValue(resultJson, new TypeReference<IdTokenDTO>() {
				});
				System.out.println("idTokenDTO:" + idTokenDTO);

				String accessToken = googleLoginResponse.getAccessToken();
				long expiresIn = Long.parseLong(googleLoginResponse.getExpiresIn());
				String sessionId = httpServletRequest.getSession().getId();
				// SecurityContextHolder에 email, accessToken저장
				userService.setRedis(accessToken, expiresIn, sessionId);

				// 이메일 존재 여부
				boolean exists = userService.existsByEmail(idTokenDTO.getEmail());
				// email 존재 시
				if (exists) {
					Long userId = userService.findByEmail(idTokenDTO.getEmail()).getUser_id();
					// email존재하지만 refreshToken값이 들어올 경우
					if (googleLoginResponse.getRefreshToken() != null) {
						// refreshToken 업데이트
						tokenService.updateRefreshToken(userId, googleLoginResponse.getRefreshToken(), sessionId);
						FirstLoginResponse firstLoginResponse = userService.firstLoginResponse(idTokenDTO,
								googleLoginResponse, sessionId);

						// refreshToken이 포함된 DTO로 전달
						return ResponseEntity.ok().body(firstLoginResponse);
					} else { // email존재, refreshToken값이 들어오지 않을때
						// sessionId 업데이트s
						tokenService.updateSessionId(userId, sessionId);
						LoginResponse loginResponse = userService.loginResponse(idTokenDTO, googleLoginResponse,
								sessionId);
						System.out.println("123==" + ResponseEntity.ok().body(loginResponse));
						// refreshToken이 없는 DTO로 전달
						return ResponseEntity.ok().body(loginResponse);
					}
				} else {
					// 유저 처음 로그인 시
					// User 테이블에 저장
					User newUser = userService.createUser(idTokenDTO);
					// refresh토큰 저장
					tokenService.saveToken(newUser.getUser_id(), googleLoginResponse, sessionId);
					FirstLoginResponse firstLoginResponse = userService.firstLoginResponse(idTokenDTO,
							googleLoginResponse, sessionId);

					System.out.println("123==" + ResponseEntity.ok().body(firstLoginResponse));
					// refreshToken이 포함된 DTO로 전달
					return ResponseEntity.ok().body(firstLoginResponse);
				}

			} else {
				throw new Exception("정보 호출에 실패했습니다.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.badRequest().body(null);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader("Session-Id") String sessionId) {
	    try {
	        System.out.println("sessionid : " + sessionId);
	        Token token = tokenRepository.findBySessionId(sessionId);
	        System.out.println("token : " + token);
	        if (token != null) {
	            Long userId = token.getUser().getUser_id();
	            System.out.println("userId : " + userId);
	            // 사용자의 sessionId를 NULL로 설정하고 modifiedDate를 업데이트합니다.
	            tokenRepository.deleteSessionId(userId, Instant.now());
	            System.out.println("DBdelete");
	            // Redis에서 세션 정보를 삭제합니다.
	            userService.deleteRedis(sessionId);
	            System.out.println("Redisdelete");
	            return ResponseEntity.ok().build(); // body 없이 상태 코드만 반환
	        } else {
	            return ResponseEntity.badRequest().build(); // body 없이 상태 코드만 반환
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // body 없이 상태 코드만 반환
	    }
	}


}