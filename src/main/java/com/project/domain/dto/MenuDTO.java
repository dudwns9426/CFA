package com.project.domain.dto;

import lombok.Data;

@Data
public class MenuDTO {
	private long no;
	private	String name;
	private String enName;
	private String explanation;
	private int searchCount;
}
