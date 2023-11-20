package com.project.domain.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class AccessTokenInfo implements Serializable {
    private String accessToken; 
    
    @JsonCreator
    public AccessTokenInfo(@JsonProperty("accessToken")String accessToken) {
        this.accessToken = accessToken;        
    }

}
