package com.project.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class LoginResponse {
	private String accessToken;
	private String email;
	private String picture;
	private String sessionId;
}
