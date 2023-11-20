package com.project.service;

import java.util.Collections;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.project.domain.dto.MenuDTO;
import com.project.domain.dto.WishlistCountDTO;
import com.project.domain.dto.WishlistDTO;
import com.project.mapper.MenuMapper;
import com.project.mapper.WishlistMapper;
import com.project.util.ExceptionUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WishlistServiceImpl implements WishlistService {
	private final WishlistMapper wishlistMapper;

	@Override
	public boolean insertAndDeleteWishlist(WishlistDTO params) {
		try {
            // 유저,메뉴 id가 null이면 삽입하지 않음
            if (params.getUserId() == null || params.getMenuId() == null) {
                return false;
            }
            // 기존에 있는 찜목록인지 확인
            int selectCount = wishlistMapper.checkWishlist(params);
            System.out.print("selectCount:"+selectCount);
            // 메뉴가 없으면 insert
            if (selectCount == 0) {
                int queryResult = wishlistMapper.insertWishlist(params);
                return queryResult == 1;
            } else {
            	int queryResult = wishlistMapper.deleteWishlist(params);
            	return queryResult == 1;
            }
        } catch (Exception e) {
            // 에러 로그를 출력
            e.printStackTrace(); 
            return false;
        }
    }
	
	@Override
	public boolean wishYn(WishlistDTO params) {
		try {
            // 유저,메뉴 id가 null이면 삽입하지 않음
            if (params.getUserId() == null || params.getMenuId() == null) {
                throw new ExceptionUtil("유저 아이디 또는 메뉴 아이디가 null입니다");
            }
            // 기존에 있는 찜목록인지 확인
            int selectCount = wishlistMapper.checkWishlist(params);
            // 메뉴가 없으면 insert
            if (selectCount == 0) {
                return false;
            } else {
            	return true;
            }
        } catch (Exception e) {
            // 에러 로그를 출력
            e.printStackTrace(); 
            return false;
        }
    }

	@Override
	public int selectWishlistTotalCount(WishlistDTO params) {
		 if (params.getMenuId() == null) {
             return 0;
         }
		return wishlistMapper.selectWishlistTotalCount(params.getMenuId());
	}

	@Override
	public List<MenuDTO> selectWishlist(String email) {
		// TODO Auto-generated method stub
		List<MenuDTO> wishlist = Collections.emptyList();
		
		wishlist = wishlistMapper.selectWishlist(email);
		
		return wishlist;
	}
	

	
	
	
}
