package com.project.service;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.project.domain.dto.AccessTokenInfo;
import com.project.domain.dto.GoogleLoginRequest;
import com.project.domain.dto.IdTokenDTO;
import com.project.domain.dto.TokenResponse;
import com.project.domain.dto.response.FirstLoginResponse;
import com.project.domain.dto.response.LoginResponse;
import com.project.domain.entity.User;
//import com.project.repository.CustomSecurityContextRepository;
import com.project.repository.UserRepository;
import com.project.util.ConfigUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	private final ConfigUtils configUtils;
//	private final CustomSecurityContextRepository securityContextRepository;
	
	@Autowired
	private StringRedisTemplate redisTemplate;

	// 인가코드를 받아 구글로그인에 request할 params를 만듬
	public GoogleLoginRequest makeRequest(String authCode) {
		GoogleLoginRequest requestParams = GoogleLoginRequest.builder().clientId(configUtils.getGoogleClientId())
				.clientSecret(configUtils.getGoogleSecret()).code(authCode)
				.redirectUri(configUtils.getGoogleRedirectUri()).grantType("authorization_code").build();
		return requestParams;
	}

	// 구글api에 requestParams로 요청하여 api로부터 Json형태로 response를 받음
	// RestTemplate 사용해서 요청
	public ResponseEntity<String> apiResponseJson(GoogleLoginRequest requestParams) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<GoogleLoginRequest> httpRequestEntity = new HttpEntity<>(requestParams, headers);
		ResponseEntity<String> apiResponseJson = restTemplate.postForEntity(configUtils.getGoogleAuthUrl() + "/token",
				httpRequestEntity, String.class);
		return apiResponseJson;
	}

	// Json 형태의 response를 자바 객체로 변환
	public TokenResponse googleLoginResponse(ResponseEntity<String> apiResponseJson)
			throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL이 아닌 값만 응답받기(NULL인 경우는 생략)

		TokenResponse googleLoginResponse = objectMapper.readValue(apiResponseJson.getBody(),
				new TypeReference<TokenResponse>() {
				});

		return googleLoginResponse;
	}

	// RestTemplate사용해서 IdToken으로 사용자 정보 받아옴
	public String resultJson(TokenResponse googleLoginResponse) {
		RestTemplate restTemplate = new RestTemplate();
		String idToken = googleLoginResponse.getIdToken();
		// JWT idToken을 전달해 JWT 저장된 사용자 정보 확인
		String requestUrl = UriComponentsBuilder.fromHttpUrl(configUtils.getGoogleAuthUrl() + "/tokeninfo")
				.queryParam("id_token", idToken).toUriString();

		String resultJson = restTemplate.getForObject(requestUrl, String.class);

		return resultJson;
	}

	public FirstLoginResponse firstLoginResponse(IdTokenDTO idTokenDTO, TokenResponse tokenResponse,String sessionId) {
		String email = idTokenDTO.getEmail();
		String picture = idTokenDTO.getPicture();
		String accessToken = tokenResponse.getAccessToken();
		String refreshToken = tokenResponse.getRefreshToken();

		FirstLoginResponse firstLoginResponse = new FirstLoginResponse(accessToken, refreshToken, email, picture,sessionId);

		return firstLoginResponse;
	}

	public LoginResponse loginResponse(IdTokenDTO idTokenDTO, TokenResponse tokenResponse, String sessionId) {
		String email = idTokenDTO.getEmail();
		String picture = idTokenDTO.getPicture();
		String accessToken = tokenResponse.getAccessToken();

		LoginResponse loginResponse = new LoginResponse(accessToken, email, picture, sessionId);

		return loginResponse;
	}

	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	
	public User createUser(IdTokenDTO idTokenDTO) {
	    String email = idTokenDTO.getEmail();
	    String locale = idTokenDTO.getLocale();

	    User newUser = User.builder().email(email).locale(locale).build();

	    return userRepository.save(newUser);
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public void setRedis(String accessToken, long expiresIn, String sessionId) {
	    AccessTokenInfo accessTokenInfo = new AccessTokenInfo(accessToken);
	    
	    System.out.println("sessionId:"+sessionId);
	    String accessTokenInfoJson = convertAccessTokenInfoToJson(accessTokenInfo);
	    System.out.println("accessTokenInfoJson:"+accessTokenInfoJson);
	    redisTemplate.opsForValue().set(sessionId, accessTokenInfoJson);
	    redisTemplate.expire(sessionId, expiresIn, TimeUnit.SECONDS);
	}
	
	public void deleteRedis(String sessionId) {
	    if (redisTemplate.hasKey(sessionId)) {
	        redisTemplate.delete(sessionId);
	        System.out.println("Deleted data for sessionId: " + sessionId);
	    } else {
	        System.out.println("No data found for sessionId: " + sessionId);
	    }
	}

	
	private String convertAccessTokenInfoToJson(AccessTokenInfo accessTokenInfo) {
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
	        return objectMapper.writeValueAsString(accessTokenInfo);
	    } catch (JsonProcessingException e) {
	        // JSON 직렬화 중 예외 처리
	        e.printStackTrace();
	        return null;
	    }
	}
	

	// 요청 헤더에서 값 추출
	// 액세스 토큰 추출
	public String extractAccessToken(HttpServletRequest request) {
		
		String accessToken = request.getHeader("Authorization");
		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			// "Bearer " 부분을 제외하고 액세스 토큰 값을 추출
			accessToken = accessToken.substring(7);
			return accessToken;
		} else {
			// 유효한 액세스 토큰이 없을 경우 예외 처리
//			throw new ExceptionUtil("No valid access token provided.");
			return null;
		}
	}
	
	
	//refreshToken추출
	public String extractRefreshToken(HttpServletRequest request) {
		String refreshToken = request.getHeader("RefreshToken");
		if(refreshToken != null) {
			return refreshToken;
		} else {
			return null;
		}
	}
	
	public String extractSessionId(HttpServletRequest request) {
        // "Session-Id" 헤더에서 세션 아이디 추출
        String sessionId = request.getHeader("Session-Id");
        return sessionId;
    }
    
    public AccessTokenInfo getAccessTokenInfoFromRedis(String sessionId) {
        String accessTokenInfoJson = redisTemplate.opsForValue().get(sessionId);
        return convertJsonToAccessTokenInfo(accessTokenInfoJson);
    }

    public AccessTokenInfo convertJsonToAccessTokenInfo(String accessTokenInfoJson) {
        if (accessTokenInfoJson == null) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(accessTokenInfoJson, AccessTokenInfo.class);
        } catch (JsonProcessingException e) {
            // JSON 역직렬화 중 예외 처리
            e.printStackTrace();
            return null;
        }
    }
	
}
