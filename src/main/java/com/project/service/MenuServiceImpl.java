package com.project.service;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.dto.MenuDTO;
import com.project.mapper.MenuMapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MenuServiceImpl implements MenuService {
    
    private final MenuMapper menuMapper;
    
    
    
    @Transactional //insert
    public boolean insertMenu(MenuDTO params) {
        try {
            // 메뉴 이름이 null이면 삽입하지 않음
            if (params.getName() == null || params.getEnName() == null) {
                return false;
            }
            // 기존에 있는 메뉴인지 확인 후
            int selectCount = menuMapper.selectMenuTotalCount(params);
            
            // 메뉴가 없으면 insert
            if (selectCount == 0) {
                int queryResult = menuMapper.insertMenu(params);
                return queryResult == 1;
            }
        } catch (Exception e) {
            // 에러 로그를 출력
            e.printStackTrace(); 
        }

        return false; // 작업 실패 시 false 반환
    }

    @Transactional // (searchCount +1) update
    public boolean updateMenu(MenuDTO params) {
        try {
            int selectCount = menuMapper.selectMenuTotalCount(params);

            if (selectCount == 0) {
                return false; // 메뉴가 존재하지 않으면 업데이트할 수 없음
            } else {
                MenuDTO menu = menuMapper.selectMenuDetail(params.getName());

                if (menu.getNo() != 0 &&  // no가 0이 아니고,
                    params.getName().equals(menu.getName())) { // 받은 이름값과 DB의 이름값이 일치하면
                	
                    params.setSearchCount(menu.getSearchCount() + 1); //DB의 search_count를 1 증가 시키는 업데이트를 진행
                    int queryResult = menuMapper.updateMenu(params);
                    return queryResult == 1;
                }
            }
        } catch (Exception e) {
            // 에러 로그를 출력
            e.printStackTrace(); 
        }

        return false; // 작업 실패 시 false 반환
    }

    
    public MenuDTO getMenuDetail(String name) {
        return menuMapper.selectMenuDetail(name);
    }
    
    //삭제
    public boolean deleteMenu(String name) {
        int queryResult = 0;
        MenuDTO menu = menuMapper.selectMenuDetail(name);

        if (menu != null) {
            queryResult = menuMapper.deleteMenu(name);
        }

        return (queryResult == 1)? true : false;
    }
    
    //한글 리스트 받기(랜덤 뽑기 용)
    public List<MenuDTO> getMenuNameList(MenuDTO params) {
        List<MenuDTO> menuNameList = Collections.emptyList();
                
        menuNameList = menuMapper.selectMenuList(params);
       
        return menuNameList;
    }
    
    // 영어이름 리스트 받기(사이드바 카테고리 리스트용)
	public List<MenuDTO> getMenuEnNameList(MenuDTO params) {
		List<MenuDTO> menuEnNameList = Collections.emptyList();

		menuEnNameList = menuMapper.selectMenuEnList(params);
	
		// TODO Auto-generated method stub
		return menuEnNameList;
	}

}