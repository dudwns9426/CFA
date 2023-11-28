package com.project.service;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.domain.dto.MenuDTO;
import com.project.mapper.MenuMapper;

import lombok.RequiredArgsConstructor;

/**
 * 메뉴 정보를 처리하는 서비스 클래스입니다.
 * 주로 메뉴 정보의 삽입, 업데이트, 삭제, 상세 조회 등의 기능을 제공합니다.
 * 
 * @author Jeon Youngjun
 */
@RequiredArgsConstructor
@Service
public class MenuServiceImpl implements MenuService {
    
    private final MenuMapper menuMapper;
    
    
    /**
     * 메뉴를 추가합니다.
     * 
     * @param params 추가할 메뉴 정보를 담은 DTO
     * @return 작업 성공 여부를 반환합니다.
     */
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

    /**
     * 메뉴의 검색 횟수를 1 증가시키고 업데이트합니다.
     * 
     * @param params 업데이트할 메뉴 정보를 담은 DTO
     * @return 작업 성공 여부를 반환합니다.
     */
    @Transactional 
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

    /**
     * 메뉴의 상세 정보를 조회합니다.
     * 
     * @param name 조회할 메뉴의 한글 이름
     * @return 조회된 메뉴의 상세 정보
     */
    public MenuDTO getMenuDetail(String name) {
        return menuMapper.selectMenuDetail(name);
    }
    
    /**
     * 메뉴를 삭제합니다.
     * 
     * @param name 삭제할 메뉴의 한글 이름
     * @return 작업 성공 여부를 반환합니다.
     */
    public boolean deleteMenu(String name) {
        int queryResult = 0;
        MenuDTO menu = menuMapper.selectMenuDetail(name);

        if (menu != null) {
            queryResult = menuMapper.deleteMenu(name);
        }

        return (queryResult == 1)? true : false;
    }
    
    /**
     * 모든 메뉴의 한글 이름을 반환합니다.(랜덤 뽑기 사용)
     * 
     * @param params 메뉴 정보를 담은 DTO
     * @return 모든 메뉴의 한글 이름이 담긴 리스트
     */
    public List<MenuDTO> getMenuNameList(MenuDTO params) {
        List<MenuDTO> menuNameList = Collections.emptyList();
                
        menuNameList = menuMapper.selectMenuList(params);
       
        return menuNameList;
    }
    
    /**
     * 모든 메뉴의 영어 이름을 반환합니다.(메뉴 사전 사용)
     * 
     * @param params 메뉴 정보를 담은 DTO
     * @return 모든 메뉴의 영어 이름이 담긴 리스트
     */
	public List<MenuDTO> getMenuEnNameList(MenuDTO params) {
		List<MenuDTO> menuEnNameList = Collections.emptyList();

		menuEnNameList = menuMapper.selectMenuEnList(params);
	
		// TODO Auto-generated method stub
		return menuEnNameList;
	}

}