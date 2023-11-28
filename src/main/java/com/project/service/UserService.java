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

/**
 * 사용자 서비스 클래스입니다. Google 로그인 및 사용자 정보 처리를 담당합니다.
 *
 * 주로 Google OAuth2를 사용하여 로그인한 사용자의 정보를 처리하고, Redis를 통해 액세스토큰을 관리합니다.
 *
 * @author Jeon Youngjun
 */
@RequiredArgsConstructor
@Service
public class UserService {
	private final UserRepository userRepository;
	private final ConfigUtils configUtils;
	
	@Autowired
	private StringRedisTemplate redisTemplate;

    /**
     * 인가코드를 받아 구글 로그인 토큰 요청에 필요한 requestParams를 생성합니다.
     *
     * @param authCode 구글에서 받은 인가코드
     * @return GoogleLoginRequest 객체
     */
	public GoogleLoginRequest makeRequest(String authCode) {
		GoogleLoginRequest requestParams = GoogleLoginRequest.builder()
				.clientId(configUtils.getGoogleClientId())
				.clientSecret(configUtils.getGoogleSecret()).code(authCode)
				.redirectUri(configUtils.getGoogleRedirectUri())
				.grantType("authorization_code")
				.build();
		return requestParams;
	}

    /**
     * Google API에 requestParams를 사용하여 요청하고, JSON 형태로 응답을 받습니다.
     *
     * @param requestParams Google 로그인 요청 파라미터
     * @return JSON 형태의 응답
     */
	public ResponseEntity<String> apiResponseJson(GoogleLoginRequest requestParams) {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<GoogleLoginRequest> httpRequestEntity = new HttpEntity<>(requestParams, headers);
		ResponseEntity<String> apiResponseJson = restTemplate.postForEntity(configUtils.getGoogleAuthUrl() + "/token",
				httpRequestEntity, String.class);
		return apiResponseJson;
	}

    /**
     * JSON 형태의 응답을 TokenResponse 객체로 변환합니다.
     *
     * @param apiResponseJson JSON 형태의 응답
     * @return TokenResponse 객체
     * @throws JsonMappingException JSON 매핑 예외
     * @throws JsonProcessingException JSON 처리 예외
     */
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

	/**
     * RestTemplate을 사용하여 IdToken으로 사용자 정보를 받아옵니다.
     *
     * @param googleLoginResponse Google 로그인 응답 정보
     * @return JSON 형태의 결과
     */
	public String resultJson(TokenResponse googleLoginResponse) {
		RestTemplate restTemplate = new RestTemplate();
		String idToken = googleLoginResponse.getIdToken();
		// JWT idToken을 전달해 JWT 저장된 사용자 정보 확인
		String requestUrl = UriComponentsBuilder.fromHttpUrl(configUtils.getGoogleAuthUrl() + "/tokeninfo")
				.queryParam("id_token", idToken).toUriString();

		String resultJson = restTemplate.getForObject(requestUrl, String.class);

		return resultJson;
	}

    /**
     * 리프레시 토큰이 포함된 로그인 시 응답할 FirstLoginResponse 객체를 생성합니다.
     *
     * @param idTokenDTO       IdTokenDTO 객체
     * @param tokenResponse    Google 로그인 응답 정보
     * @param sessionId         세션 ID
     * @return FirstLoginResponse 객체
     */
	public FirstLoginResponse firstLoginResponse(IdTokenDTO idTokenDTO, TokenResponse tokenResponse,String sessionId) {
		String email = idTokenDTO.getEmail();
		String picture = idTokenDTO.getPicture();
		String accessToken = tokenResponse.getAccessToken();
		String refreshToken = tokenResponse.getRefreshToken();

		FirstLoginResponse firstLoginResponse = new FirstLoginResponse(accessToken, refreshToken, email, picture,sessionId);

		return firstLoginResponse;
	}

    /**
     * 리프레시 토큰이 포함되지 않은 로그인 시 응답할 LoginResponse 객체를 생성합니다.
     *
     * @param idTokenDTO    IdTokenDTO 객체
     * @param tokenResponse Google 로그인 응답 정보
     * @param sessionId     세션 ID
     * @return LoginResponse 객체
     */
	public LoginResponse loginResponse(IdTokenDTO idTokenDTO, TokenResponse tokenResponse, String sessionId) {
		String email = idTokenDTO.getEmail();
		String picture = idTokenDTO.getPicture();
		String accessToken = tokenResponse.getAccessToken();

		LoginResponse loginResponse = new LoginResponse(accessToken, email, picture, sessionId);

		return loginResponse;
	}

    /**
     * 이메일로 사용자가 존재하는지 확인합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 존재 여부
     */
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

    /**
     * IdTokenDTO 정보를 사용하여 DB에 사용자를 갱신합니다.
     *
     * @param idTokenDTO IdTokenDTO 객체
     * @return 생성된 사용자
     */
	public User createUser(IdTokenDTO idTokenDTO) {
	    String email = idTokenDTO.getEmail();
	    String locale = idTokenDTO.getLocale();

	    User newUser = User.builder().email(email).locale(locale).build();

	    return userRepository.save(newUser);
	}

    /**
     * 이메일로 사용자를 찾습니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 객체
     */
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

    /**
     * Redis에 액세스 토큰 정보를 저장합니다.
     *
     * @param accessToken 액세스 토큰
     * @param expiresIn   토큰 유효 기간 (1시간)
     * @param sessionId   세션 ID
     */
	public void setRedis(String accessToken, long expiresIn, String sessionId) {
	    AccessTokenInfo accessTokenInfo = new AccessTokenInfo(accessToken);
	    
	    System.out.println("sessionId:"+sessionId);
	    String accessTokenInfoJson = convertAccessTokenInfoToJson(accessTokenInfo);
	    System.out.println("accessTokenInfoJson:"+accessTokenInfoJson);
	    redisTemplate.opsForValue().set(sessionId, accessTokenInfoJson);
	    redisTemplate.expire(sessionId, expiresIn, TimeUnit.SECONDS);
	}
	
    /**
     * Redis에서 세션 정보를 삭제합니다.
     *
     * @param sessionId 세션 ID
     * @author Kim Taewon
     */
	public void deleteRedis(String sessionId) {
	    if (redisTemplate.hasKey(sessionId)) {
	        redisTemplate.delete(sessionId);
	        System.out.println("Deleted data for sessionId: " + sessionId);
	    } else {
	        System.out.println("No data found for sessionId: " + sessionId);
	    }
	}

    /**
     * 액세스 토큰 정보를 JSON 형태로 변환합니다.
     *
     * @param accessTokenInfo 액세스 토큰 정보 객체
     * @return JSON 형태의 액세스 토큰 정보
     */
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
	

    /**
     * 요청 헤더에서 액세스 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 액세스 토큰
     */
	public String extractAccessToken(HttpServletRequest request) {
		
		String accessToken = request.getHeader("Authorization");
		if (accessToken != null && accessToken.startsWith("Bearer ")) {
			// "Bearer " 부분을 제외하고 액세스 토큰 값을 추출
			accessToken = accessToken.substring(7);
			return accessToken;
		} else {
			// 유효한 액세스 토큰이 없을 경우 재로그인 요구
			return null;
		}
	}
	
	
    /**
     * 요청 헤더에서 Refresh 토큰을 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 Refresh 토큰
     */
	public String extractRefreshToken(HttpServletRequest request) {
		String refreshToken = request.getHeader("RefreshToken");
		if(refreshToken != null) {
			return refreshToken;
		} else {
			return null;
		}
	}
	
    /**
     * 요청 헤더에서 세션 ID를 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 추출된 세션 ID
     */
	public String extractSessionId(HttpServletRequest request) {
        // "Session-Id" 헤더에서 세션 아이디 추출
        String sessionId = request.getHeader("Session-Id");
        return sessionId;
    }
    
    /**
     * Redis에서 세션 정보로부터 AccessTokenInfo 객체를 가져옵니다.
     *
     * @param sessionId 세션 ID
     * @return AccessTokenInfo 객체
     */
    public AccessTokenInfo getAccessTokenInfoFromRedis(String sessionId) {
        String accessTokenInfoJson = redisTemplate.opsForValue().get(sessionId);
        return convertJsonToAccessTokenInfo(accessTokenInfoJson);
    }

    /**
     * JSON 형태의 문자열을 AccessTokenInfo 객체로 변환합니다.
     *
     * @param accessTokenInfoJson JSON 형태의 문자열
     * @return AccessTokenInfo 객체
     */
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
