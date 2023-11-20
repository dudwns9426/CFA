package com.project.domain.dto;

import java.util.List;

import com.project.domain.entity.RestaurantInfo;

import lombok.Data;

@Data
public class OutputTextDTO {
	private int type;
	private	String text;
	private List<RestaurantInfo> data;
}
