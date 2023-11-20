package com.project.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.project.domain.dto.IntReviewDTO;
import com.project.domain.dto.ReviewDTO;
import com.project.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService{
	
	private final ReviewMapper reviewMapper;
	
	public long findIdx(String foodName) {
		return reviewMapper.findIdx(foodName);
	}
	
	public boolean insertReview(IntReviewDTO review) {
		try {
			long abc = reviewMapper.insertReview(review);
			if(abc == 1L) {
				return true;
			}else {
				return false;
			}
		}catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	}
	
	public boolean deleteReview(long idx) {
		try {
			reviewMapper.deleteReview(idx);
		}catch (Exception e) {
            e.printStackTrace();
            return false;
        }
		return true;
	}
	
	public List<ReviewDTO> getSampleReview(long foodIdx){
		List<ReviewDTO> result = reviewMapper.selectSampleList(foodIdx);
		return result;
	}
	
	public List<ReviewDTO> getAllReview(long foodIdx){
		List<ReviewDTO> result = reviewMapper.selectReviewList(foodIdx);
		return result;
	}
}
