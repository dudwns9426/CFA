package com.project.mapper;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.domain.dto.MenuDTO;
import com.project.domain.dto.WishlistDTO;
import com.project.domain.dto.WishlistRequest;

@Mapper
public interface WishlistMapper {
	 public int insertWishlist(WishlistDTO params);
	 
	 public int checkWishlist(WishlistDTO params);
	 
	 public int selectWishlistTotalCount(Long menuId);
	 
	 public int deleteWishlist(WishlistDTO params);
	 
	 public List<MenuDTO> selectWishlist(String email);
}
