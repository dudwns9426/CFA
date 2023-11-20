package com.project.domain.entity;

import lombok.Data;

@Data
public class RestaurantSearchRequest {
	private double latitude;
    private double longitude;
    private String foodName;
}
