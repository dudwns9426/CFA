package com.project.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.protobuf.util.JsonFormat;
import com.project.controller.RestaurantController;
import com.project.domain.dto.ProcssedTextDTO;
import com.project.domain.entity.RestaurantInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
	@Value("${googlemaps.api.key}")
	private String apiKey;

	private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);
	
    private final Resource credentialsResource;  // 서비스 계정 키 파일을 주입

    @SuppressWarnings("unchecked")
    public ProcssedTextDTO textNER(String inputText, String inputSession) {
        System.out.println("-----textNer 시작-----");// 로그
        ProcssedTextDTO procssedText = new ProcssedTextDTO();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> displayMap = new HashMap<String, Object>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
        	System.out.println("-----서비스키 시작-----");// 로그
            // 서비스 계정 키를 credentialsResource에서 로드
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(credentialsResource.getInputStream());
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            SessionsSettings sessionsSettings = SessionsSettings.newBuilder().setCredentialsProvider(credentialsProvider).build();
            try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            	System.out.println("-----세션전송 시작-----");// 로그
                SessionName session = SessionName.of("chatbot-401706", inputSession);
                TextInput.Builder textInput = TextInput.newBuilder().setText(inputText).setLanguageCode("en-US");
                QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();
                DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

                String jsonResponse = JsonFormat.printer().print(response);

                resultMap = objectMapper.readValue(jsonResponse, Map.class);
                resultMap = objectMapper.convertValue(resultMap.get("queryResult"), Map.class);
                procssedText.setFulfillmentText((String) resultMap.get("fulfillmentText"));

                displayMap = objectMapper.convertValue(resultMap.get("intent"), Map.class);
                procssedText.setDisplayName((String) displayMap.get("displayName"));

                resultMap = objectMapper.convertValue(resultMap.get("parameters"), Map.class);
                procssedText.setUserFoodName((String) resultMap.get("user_foodname"));
                procssedText.setUserIngredient((ArrayList<String>) resultMap.get("user_ingredient"));
                procssedText.setUserTaste((ArrayList<String>) resultMap.get("user_taste"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return procssedText;
    }

    public List<RestaurantInfo> getRestaurants(@RequestParam double latitude, @RequestParam double longitude,
			@RequestParam String foodName) {
		List<RestaurantInfo> restaurantInfoList = new ArrayList<>();
		List<RestaurantInfo> restaurantInfoTop5 = new ArrayList<>();
		try {
			LatLng location = new LatLng(latitude, longitude);
			GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();

			PlacesSearchResponse response = PlacesApi.textSearchQuery(context, foodName).location(location).radius(1000)
					.type(PlaceType.RESTAURANT).language("en").await();

			DecimalFormat df = new DecimalFormat("#.00");

			List<LatLng> destinationCoordinates = new ArrayList<>();
			for (PlacesSearchResult result : response.results) {
				destinationCoordinates.add(new LatLng(result.geometry.location.lat, result.geometry.location.lng));
			}

			for (int i = 0; i < response.results.length; i++) {
				PlacesSearchResult result = response.results[i];

				double distanceInMeters = Math.round(calManhattanDistanceInMeters(latitude, longitude,
						result.geometry.location.lat, result.geometry.location.lng) * 10.0) / 10.0; // 맨해튼 거리 구하기
				try {
					double rating = getRatingByPlaceId(context, result.placeId);
					int reviewCount = getReviewCountByPlaceId(context, result.placeId);

					if (rating >= 4.0 && reviewCount >= 60 && "OPERATIONAL".equals(result.businessStatus)) {
						restaurantInfoList.add(new RestaurantInfo(result.name, df.format(rating), reviewCount,
								distanceInMeters, result.placeId));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Collections.sort(restaurantInfoList, Comparator.comparingDouble(RestaurantInfo::getDistanceInMeters));

		} catch (Exception e) {
			e.printStackTrace();
		}

		restaurantInfoTop5 = restaurantInfoList.subList(0, Math.min(5, restaurantInfoList.size()));
		logger.info("POST response: {}", restaurantInfoTop5); // 반환하는 음식점 리스트 log

		return restaurantInfoTop5;
	}

	private double getRatingByPlaceId(GeoApiContext context, String placeId) throws Exception {
		return PlacesApi.placeDetails(context, placeId).await().rating;
	}

	private int getReviewCountByPlaceId(GeoApiContext context, String placeId) throws Exception {
		return PlacesApi.placeDetails(context, placeId).await().userRatingsTotal;
	}

	private double calManhattanDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {

		final int R = 6371; // 지구 반지름 (km)
		double dLat = Math.abs(Math.toRadians(lat2 - lat1));
		double dLon = Math.abs(Math.toRadians(lon2 - lon1));

		double distance = dLat + dLon;
		double distanceInKm = R * distance;
		return distanceInKm * 1000;
	}

	public String sendproperty(ArrayList<String> inputIngredient, ArrayList<String> inputTaste) {
		String url = "http://43.202.150.249:8888/get_matching_menu"; // POST 요청을 보낼 URL을 지정

		// JSON 데이터를 포함하는 객체 생성
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// RestTemplate 객체 생성
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> request = new HashMap<>();
		request.put("taste", String.join(",",inputTaste));
		request.put("made_with", String.join(",",inputIngredient));
		
		// POST 요청 보내기
		ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

		// 서버로부터 받은 응답 데이터
		String str = response.getBody();
		
		return str;
	}
}