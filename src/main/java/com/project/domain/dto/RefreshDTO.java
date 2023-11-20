package com.project.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RefreshDTO {
	@JsonProperty("access_token")
	private String accessToken;
	@JsonProperty("expires_in")
	private String expiresIn;
	private String scope;
	@JsonProperty("token_type")
	private String tokenType;
	@JsonProperty("id_token")
	private String idToken;
}
