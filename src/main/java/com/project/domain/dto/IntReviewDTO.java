package com.project.domain.dto;

import lombok.Data;

@Data
public class IntReviewDTO {
	String writer;
	String content;
	long foodIdx;
	
	
	public IntReviewDTO(String writer, String content, long foodIdx) {
		this.writer = writer;
		this.content = content;
		this.foodIdx = foodIdx;
	}
}
