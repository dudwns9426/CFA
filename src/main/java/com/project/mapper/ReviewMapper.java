package com.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.domain.dto.IntReviewDTO;
import com.project.domain.dto.ReviewDTO;

@Mapper
public interface ReviewMapper {
	
	public long findReviewId(String foodName);
	
	public long deleteReview(long reviewId);
	  
	public List<ReviewDTO> selectReviewList(long menuId);
	
	public List<ReviewDTO> selectSampleList(long menuId);

	public long insertReview(IntReviewDTO intReviewDTO);
}
