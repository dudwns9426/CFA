package com.project.domain.dto;

import lombok.Data;

@Data
public class ReviewDTO {
	int idx;
	String writer;
	String content;
	String deleteYn;
	String dateW;
	String foodName;
}
