package com.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.project.domain.dto.MenuDTO;

@Mapper
public interface MenuMapper {
	public int insertMenu(MenuDTO params);

	public MenuDTO selectMenuDetail(String name);

	public int updateMenu(MenuDTO params);

	public int deleteMenu(String name);

	public List<MenuDTO> selectMenuList(MenuDTO params);

	public int selectMenuTotalCount(MenuDTO params);

	public List<MenuDTO> selectMenuEnList(MenuDTO params);

}
