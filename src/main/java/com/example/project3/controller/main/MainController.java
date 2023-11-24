package com.example.project3.controller.main;

import com.example.project3.service.mainApiService.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class MainController {

    private final WeatherService weatherService;

    @Autowired
    public MainController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    // 특정 위치의 현재 날씨 조회
    @GetMapping("/current/{locationId}")
    public ResponseEntity<String> getCurrentWeather(@PathVariable int locationId) {
        // 가정: locationId를 사용하여 latitude와 longitude를 계산했습니다.
        double latitude = calculateLatitude(locationId);
        double longitude = calculateLongitude(locationId);

        // getWeatherByCoordinates 메서드에 두 개의 인자를 전달
        String currentWeather = weatherService.getWeatherByCoordinates(latitude, longitude);

        return ResponseEntity.ok(currentWeather);
    }

    private double calculateLatitude(int locationId) {
        // locationId를 사용하여 latitude 계산
        // 실제 구현에 따라 다르게 구현 가능
        return 0.0; // 예시로 0.0을 반환
    }

    private double calculateLongitude(int locationId) {
        // locationId를 사용하여 longitude 계산
        // 실제 구현에 따라 다르게 구현 가능
        return 0.0; // 예시로 0.0을 반환
    }

    // 특정 날짜의 날씨 조회
    @GetMapping("/forecast/{date}")
    public ResponseEntity<String> getWeatherForecast(@PathVariable String date) {
        String weatherForecast = weatherService.getWeatherForecast(date);
        return ResponseEntity.ok(weatherForecast);
    }

    // 경도와 위도로 날씨 조회
    @GetMapping("/coordinates")
    public ResponseEntity<String> getWeatherByCoordinates(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        String weatherByCoordinates = weatherService.getWeatherByCoordinates(latitude, longitude);
        return ResponseEntity.ok(weatherByCoordinates);
    }

}
