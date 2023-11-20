package com.project.domain.entity;

import lombok.Data;

@Data
public class RestaurantInfo {
	private String name;
	private String rating;
	private int reviewCount;
	private double distanceInMeters;
	private String placeId;	

	public RestaurantInfo(String name, String rating, int reviewCount, double distanceInMeters, String placeId) {
		this.name = name;
		this.rating = rating;
		this.reviewCount = reviewCount;
		this.distanceInMeters = distanceInMeters;
		this.placeId = placeId;
	}
}
