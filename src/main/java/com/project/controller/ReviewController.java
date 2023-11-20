package com.project.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.domain.dto.IntReviewDTO;
import com.project.domain.dto.ReviewDTO;
import com.project.service.ReviewService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ReviewController {
	
	private final ReviewService reviewService;
	
	@PostMapping("/review")
	public ResponseEntity<Void> insertreview(@RequestBody ReviewDTO review) {
		reviewService.insertReview(new IntReviewDTO(review.getWriter(),review.getContent(),reviewService.findMenuId(review.getFoodName())));
		
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/review/{reviewId}")
	public boolean deleteReview(@PathVariable long reviewId) {
	    boolean result;
	    
	    result = reviewService.deleteReview(reviewId);
	    
	    return result;
	}
	
	@GetMapping("/samplereview")
	public List<ReviewDTO> getSampleList(@RequestParam String foodName){
		long menuId = reviewService.findMenuId(foodName);
		List<ReviewDTO> result = reviewService.getSampleReview(menuId);
		return result;
	}
	
	@GetMapping("/allreview")
	public List<ReviewDTO> getAllList(@RequestParam String foodName){
		long menuId = reviewService.findMenuId(foodName);
		System.out.println("-----" + menuId + "-----");
		List<ReviewDTO> result = reviewService.getAllReview(menuId);
		System.out.println(result);
		return result;
	}
}
