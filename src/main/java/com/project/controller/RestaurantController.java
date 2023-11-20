package com.project.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.project.domain.dto.InputTextDTO;
import com.project.domain.dto.OutputTextDTO;
import com.project.domain.dto.ProcssedTextDTO;
import com.project.service.RestaurantServiceImpl;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class RestaurantController {
	private final RestaurantServiceImpl restaurantService;
	
    @PostMapping("/restaurants")
    public OutputTextDTO mainStream(HttpServletRequest request, @RequestBody InputTextDTO inputText) {
    	OutputTextDTO outPut = new OutputTextDTO(); 
        ProcssedTextDTO processedInput = restaurantService.textNER(inputText.getText(), request.getSession().getId());
        
        if("input_property".equals(processedInput.getDisplayName())) {
        	outPut.setText(restaurantService.sendproperty(processedInput.getUserIngredient(), processedInput.getUserTaste()));
        	outPut.setType(2);
        }else if("input_property_yes".equals(processedInput.getDisplayName())) {
        	outPut.setText("property_yes : " + processedInput.getFulfillmentText());
        	outPut.setData(restaurantService.getRestaurants(inputText.getLatitude(),inputText.getLongitude(),processedInput.getUserFoodName()));
        	outPut.setType(1);
        }else if("input_property_no".equals(processedInput.getDisplayName())) {
        	outPut.setText("property_no : " + processedInput.getFulfillmentText());
        	outPut.setType(0);
        }else if("input_food".equals(processedInput.getDisplayName())) {
        	outPut.setText("food : " + processedInput.getFulfillmentText());
        	outPut.setData(restaurantService.getRestaurants(inputText.getLatitude(),inputText.getLongitude(),processedInput.getUserFoodName()));
        	outPut.setType(1);
        }else {
        	outPut.setText(processedInput.getFulfillmentText());
        	outPut.setType(0);
        }
        
        return outPut;
    }
}

