package com.example.project3.service.mainApiService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Component
public class WeatherApiService {

    @Value("ryCNXqrBb6PzrL17tTbO4MjxBBfAaYVJhtNWxBAzddWaLNQZjWoQKLXMvqXCqpEBz%2BWaB5dSiGrs1cdCrKjeZg%3D%3D")
    private String apiKey;

    private final String apiUrl = "http://apis.data.go.kr/1360000/MidFcstInfoService";

    private final RestTemplate restTemplate;

    public WeatherApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getWeatherForecast(String location) {
        // 기상청 API를 호출하기 위한 URL 조립
        String apiUrlWithLocation = apiUrl + "?location=" + location + "&apiKey=" + apiKey;

        // API 호출 및 응답 받아오기
        String apiResponse = restTemplate.getForObject(apiUrlWithLocation, String.class);

        // TODO:실제로는 받아온 API 응답을 가공하는 로직을 추가하기
        // 여기서는 단순히 받아온 JSON 문자열을 반환하는로직임.

        return apiResponse;
    }


}
