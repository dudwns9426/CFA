package com.project.domain.dto;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ProcssedTextDTO {
	private String displayName;
	
	private String fulfillmentText;

    @SerializedName("user_foodName")
    private String userFoodName;

    @SerializedName("user_ingredient")
    private ArrayList<String> userIngredient;

    @SerializedName("user_taste")
    private ArrayList<String> userTaste;
}
