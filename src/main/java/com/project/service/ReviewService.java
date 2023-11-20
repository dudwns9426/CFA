package com.project.service;

import java.util.List;

import com.project.domain.dto.IntReviewDTO;
import com.project.domain.dto.ReviewDTO;

public interface ReviewService {
	
	public long findIdx(String name);
	
	public boolean insertReview(IntReviewDTO intReviewDTO);
	
	public boolean deleteReview(long idx);
	
	public List<ReviewDTO> getSampleReview(long foodIdx);
	
	public List<ReviewDTO> getAllReview(long foodIdx);
}
