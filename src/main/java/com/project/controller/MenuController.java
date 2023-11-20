package com.project.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.domain.dto.MenuDTO;
import com.project.service.MenuService;
import com.project.util.ExceptionUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class MenuController {
	
	private final MenuService menuService;

	//메뉴 인서트
	 @PostMapping("/menu")
	    public ResponseEntity<String> insertMenu(@RequestBody MenuDTO params) {
	        try {
	            // 메뉴 이름이 null인 경우 예외 발생
	            if (params.getName() == null || params.getEnName() == null) {
	                throw new IllegalArgumentException("메뉴 이름, 영어 이름은 포함되어야 합니다.");
	            }

	            boolean result = menuService.insertMenu(params);

	            if (result) {
	                return ResponseEntity.ok("insert 성공");
	            } else {
	            	throw new RuntimeException("insert 실패");
	            }
	        } catch (IllegalArgumentException e) {
	            // 유효성 검사 예외 처리
	            return ExceptionUtil.handleException(e);
	        } catch (Exception e) {
	            // 다른 예외 처리
	            return ExceptionUtil.handleException(e);
	        }
	    }
	 
	 @PutMapping("/menu")
	    public ResponseEntity<String> updateMenu(@RequestBody MenuDTO params) {
	        try {
	            // 메뉴 이름이 null인 경우 예외 발생
	            if (params.getName() == null || params.getEnName() == null) {
	                throw new IllegalArgumentException("메뉴 이름, 영어 이름은 포함되어야 합니다.");
	            }

	            boolean result = menuService.updateMenu(params);

	            if (result) {
	                return ResponseEntity.ok("업데이트 성공");
	            } else {
	                throw new RuntimeException("업데이트 실패");
	            }
	        } catch (IllegalArgumentException e) {
	            // 유효성 검사 예외 처리
	            return ExceptionUtil.handleException(e);
	        } catch (Exception e) {
	            // 다른 예외 처리
	            return ExceptionUtil.handleException(e);
	        }
	    }
	 
	 //랜덤 메뉴
	 @GetMapping("/randommenu")
	   public ResponseEntity<Map<String, String>> getMsg(MenuDTO params) {
	      
	       // 서비스를 통해 메뉴 이름 데이터를 가져옵니다.
	       List<MenuDTO> menuList = menuService.getMenuNameList(params);

	       // 리스트에서 랜덤한 인덱스를 계산하여 메뉴를 선택합니다.
	       int index = (int) (Math.random() * menuList.size());
	       String randomMenu = menuList.get(index).getName();

	       Map<String,String> result = new HashMap();
	       result.put("menu",randomMenu);
	       
	       // JSON 데이터를 응답으로 포함하여 HTTP 응답을 반환합니다.
	       return ResponseEntity.ok(result);
	   }
	
	@GetMapping("/menu")
	   public ResponseEntity<Map<String, List<String>>> menuEnData(MenuDTO params) {
	       // 서비스를 통해 메뉴 데이터를 가져옵니다.
	       List<MenuDTO> menuList = menuService.getMenuEnNameList(params);

	       // 메뉴 영어이름과 설명을 저장할 리스트를 초기화합니다.
	       List<String> menuEn = new ArrayList<>();
	       List<String> explanation = new ArrayList<>();

	       // 메뉴 데이터를 반복하여 메뉴 영어이름과 설명을 추출합니다.
	       for (int i = 0; i < menuList.size(); i++) {
	           menuEn.add(menuList.get(i).getEnName()); // 메뉴 영어이름 추가
	           explanation.add(menuList.get(i).getExplanation()); // 설명 추가
	       }

	       // JSON 데이터를 다루기 위한 ObjectMapper를 생성합니다.
	       ObjectMapper objectMapper = new ObjectMapper();

	       Map<String, List<String>> resultArray = new HashMap();
	       resultArray.put("menuEn", menuEn);
	       resultArray.put("explanation", explanation);
	       try {
	            String jsonString = objectMapper.writeValueAsString(resultArray);
	            System.out.println("js"+jsonString);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	       // JSON 배열을 응답으로 포함하여 HTTP 응답을 반환합니다.
	       return ResponseEntity.ok(resultArray);
	   }

	
}
