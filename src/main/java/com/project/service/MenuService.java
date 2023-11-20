package com.project.service;

import java.util.List;


import com.project.domain.dto.MenuDTO;


public interface MenuService {
	  public boolean updateMenu(MenuDTO params);

	  public MenuDTO getMenuDetail(String name);

	  public boolean deleteMenu(String name);
	  
	  public boolean insertMenu(MenuDTO params);
	  
	  public List<MenuDTO> getMenuNameList(MenuDTO params);
	  
	  public List<MenuDTO> getMenuEnNameList(MenuDTO params);
}
