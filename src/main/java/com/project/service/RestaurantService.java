package com.project.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.project.domain.dto.ProcssedTextDTO;
import com.project.domain.entity.RestaurantInfo;

public interface RestaurantService {
	public ProcssedTextDTO textNER(String inputText,String session);
	public List<RestaurantInfo> getRestaurants(@RequestParam double latitude, @RequestParam double longitude,
			@RequestParam String foodName);
	public String sendproperty(ArrayList<String> inputIngredient, ArrayList<String> inputTaste);
}
