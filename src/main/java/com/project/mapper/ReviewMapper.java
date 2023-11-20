package com.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.domain.dto.IntReviewDTO;
import com.project.domain.dto.ReviewDTO;

@Mapper
public interface ReviewMapper {
	
	public long findIdx(String foodName);
	
	public long deleteReview(long idx);
	  
	public List<ReviewDTO> selectReviewList(long foodIdx);
	
	public List<ReviewDTO> selectSampleList(long foodIdx);

	public long insertReview(IntReviewDTO intReviewDTO);
}
