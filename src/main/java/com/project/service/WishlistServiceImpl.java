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

/**
 * 찜목록 처리와 관련된 서비스 클래스입니다.
 * 주로 찜목록 추가, 삭제, 조회, 총 갯수 조회 등의 기능을 제공합니다.
 * 
 * @author Jeon Youngjun
 */
@RequiredArgsConstructor
@Service
public class WishlistServiceImpl implements WishlistService {
	private final WishlistMapper wishlistMapper;

    /**
     * 찜목록을 추가 또는 삭제합니다.
     * 
     * @param params 찜목록에 추가 또는 삭제할 정보를 담은 DTO
     * @return 작업 성공 여부를 반환합니다.
     */
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
	
    /**
     * 찜목록에 해당 메뉴가 있는지 여부를 조회합니다.
     * 
     * @param params 조회할 찜목록 정보를 담은 DTO
     * @return 찜목록에 해당 메뉴가 있으면 true, 없으면 false를 반환합니다.
     */
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
	
    /**
     * 찜목록에 해당 메뉴의 총 갯수를 조회합니다.
     * 
     * @param params 조회할 찜목록 정보를 담은 DTO
     * @return 찜목록에 해당 메뉴의 총 갯수
     */
	@Override
	public int selectWishlistTotalCount(WishlistDTO params) {
		 if (params.getMenuId() == null) {
             return 0;
         }
		return wishlistMapper.selectWishlistTotalCount(params.getMenuId());
	}

    /**
     * 찜목록에 해당 이메일의 모든 메뉴를 조회합니다.
     * 
     * @param email 조회할 찜목록의 이메일
     * @return 찜목록에 해당 이메일의 모든 메뉴 리스트
     */
	@Override
	public List<MenuDTO> selectWishlist(String email) {
		// TODO Auto-generated method stub
		List<MenuDTO> wishlist = Collections.emptyList();
		
		wishlist = wishlistMapper.selectWishlist(email);
		
		return wishlist;
	}
	
}
