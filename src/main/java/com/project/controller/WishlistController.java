package com.project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.dto.MenuDTO;
import com.project.domain.dto.WishlistCountDTO;
import com.project.domain.dto.WishlistDTO;
import com.project.domain.dto.WishlistRequest;
import com.project.domain.entity.User;
import com.project.service.MenuService;
import com.project.service.UserService;
import com.project.service.WishlistService;
import com.project.util.ExceptionUtil;

import lombok.RequiredArgsConstructor;

/**
 * 찜목록을 처리하는 컨트롤러 클래스입니다.
 * 사용자의 찜목록에 메뉴를 추가하거나 삭제하는 기능을 제공합니다.
 * 
 * @author Jeon Youngjun)
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/wishlist")
public class WishlistController {

	private final WishlistService wishlistService;
	private final UserService userService;
	private final MenuService menuService;

    /**
     * 찜목록에 메뉴를 추가하거나 삭제하는 요청을 처리합니다.
     * 
     * @param params 찜목록 요청 파라미터를 담은 객체
     * @return 찜목록에 대한 응답으로 찜 갯수와 추가 또는 삭제 여부를 반환합니다.
     */
	@PostMapping
	public ResponseEntity<WishlistCountDTO> insertDeleteWishlist(@RequestBody WishlistRequest params) {
		try {
			// 메뉴 이름이 null인 경우 예외 발생
			if (params.getEmail() == null || params.getMenuEn() == null) {
				throw new ExceptionUtil("이메일, 메뉴 영어 이름은 포함되어야 합니다.");
			}

			String email = params.getEmail();
			boolean exists = userService.existsByEmail(email);

			if (exists) {
				User user = userService.findByEmail(email);

				if (user != null) {
					Long userId = user.getUser_id();
					MenuDTO menu = menuService.getMenuDetail(params.getMenuEn());

					if (menu != null) {
						Long menuId = menu.getNo();
						WishlistDTO wishlistDTO = new WishlistDTO(userId, menuId);
						boolean result = wishlistService.insertAndDeleteWishlist(wishlistDTO);

						if (result) {
							int totalCount = wishlistService.selectWishlistTotalCount(wishlistDTO);
							boolean wishYn = wishlistService.wishYn(wishlistDTO);
							WishlistCountDTO wishlistCountDTO = new WishlistCountDTO();
							
							wishlistCountDTO.setCount(totalCount);
							wishlistCountDTO.setWishYn(wishYn);
							return ResponseEntity.ok(wishlistCountDTO);
						}
					}
				}
			} else {
				throw new ExceptionUtil("존재하지 않는 이메일 입니다.");
			}
		} catch (Exception e) {
			// 다른 예외 처리
			throw new ExceptionUtil("예외가 발생했습니다.");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}
	
    /**
     * 찜목록의 메뉴 갯수와 추가 여부를 반환합니다.
     * 
     * @param params 찜목록 요청 파라미터를 담은 객체
     * @return 해당 메뉴에 대한 응답으로 전체 사용자의 찜한 갯수와 현재 사용자의 추가 여부를 반환합니다.
     */
	@PostMapping("/oncreate")
	public ResponseEntity<WishlistCountDTO> getWishlistCount(@RequestBody WishlistRequest params) {
		try {
			// 메뉴 이름이 null인 경우 예외 발생
			if (params.getEmail() == null || params.getMenuEn() == null) {
				throw new ExceptionUtil("이메일, 메뉴 영어 이름은 포함되어야 합니다.");
			}

			String email = params.getEmail();
			boolean exists = userService.existsByEmail(email);

			if (exists) {
				User user = userService.findByEmail(email);

				if (user != null) {
					Long userId = user.getUser_id();
					MenuDTO menu = menuService.getMenuDetail(params.getMenuEn());

					if (menu != null) {
						Long menuId = menu.getNo();
						WishlistDTO wishlistDTO = new WishlistDTO(userId, menuId);
						
							int totalCount = wishlistService.selectWishlistTotalCount(wishlistDTO);
							boolean wishYn = wishlistService.wishYn(wishlistDTO);
							WishlistCountDTO wishlistCountDTO = new WishlistCountDTO();
							
							wishlistCountDTO.setCount(totalCount);
							wishlistCountDTO.setWishYn(wishYn);
							return ResponseEntity.ok(wishlistCountDTO);
						
					}
				}
			} else {
				throw new ExceptionUtil("존재하지 않는 이메일 입니다.");
			}
		} catch (Exception e) {
			// 다른 예외 처리
			throw new ExceptionUtil("예외가 발생했습니다.");
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
	}

    /**
     * 사용자의 위시리스트에 등록된 메뉴 정보를 반환합니다.
     * 
     * @param email 사용자 이메일
     * @return 위시리스트에 등록된 메뉴 영어 이름과 설명을 반환합니다.
     */
	@GetMapping
	public ResponseEntity<Map<String, List<String>>> getWishlist(@RequestHeader("email") String email) {
		if (email == null) {
			throw new ExceptionUtil("이메일 값이 넘어오지 않았습니다.");
		}
		List<MenuDTO> menuList = wishlistService.selectWishlist(email);

		List<String> menuEn = new ArrayList<>();
		List<String> explanation = new ArrayList<>();

		for (int i = 0; i < menuList.size(); i++) {
			menuEn.add(menuList.get(i).getEnName()); // 메뉴 영어이름 추가
			explanation.add(menuList.get(i).getExplanation()); // 설명 추가
		}
		ObjectMapper objectMapper = new ObjectMapper();

		Map<String, List<String>> resultArray = new HashMap();
		resultArray.put("menuEn", menuEn);
		resultArray.put("explanation", explanation);
		try {
			String jsonString = objectMapper.writeValueAsString(resultArray);
			System.out.println("js" + jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// JSON 배열을 응답으로 포함하여 HTTP 응답을 반환합니다.
		return ResponseEntity.ok(resultArray);

	}
}
