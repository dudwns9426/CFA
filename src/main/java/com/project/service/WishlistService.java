package com.project.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.project.domain.dto.MenuDTO;
import com.project.domain.dto.WishlistCountDTO;
import com.project.domain.dto.WishlistDTO;
import com.project.domain.dto.WishlistRequest;


public interface WishlistService {
	public boolean insertAndDeleteWishlist(WishlistDTO params);
	
	public boolean wishYn(WishlistDTO params);
	
	public int selectWishlistTotalCount(WishlistDTO params);
	
	public List<MenuDTO> selectWishlist(String email);

	
}
