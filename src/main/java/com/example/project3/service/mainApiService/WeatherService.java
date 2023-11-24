package com.example.project3.service.mainApiService;



import com.example.project3.Entity.main.Weather;
import com.example.project3.repository.main.WeatherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class WeatherService {

    private final WeatherRepository weatherRepository;

    @Autowired
    public WeatherService(WeatherRepository weatherRepository) {

        this.weatherRepository = weatherRepository;
    }

    public Optional<Weather> findLatestWeatherByLocationId(Long locationId) {
        return weatherRepository.findTopByLocation_LocationIdOrderByObservationTimeDesc(locationId);
    }

    // 특정 위치의 현재 날씨 조회
    public String getCurrentWeather(int locationId) {
        Optional<Weather> weatherOptional = findLatestWeatherByLocationId((long) locationId);

        return weatherOptional.map(weather -> "Current temperature: " + weather.getTemperature())
                .orElse("Weather information not available for the location.");
    }
    // 특정 날짜의 날씨 조회
    public String getWeatherForecast(String dateString) {
        // 날짜에 따른 날씨 조회 로직을 추가,날짜에 해당하는 날씨 정보가 없다면 메시지 반환

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateString);

            List<Weather> weatherList = weatherRepository.findByForecastDate(date);

            if (weatherList.isEmpty()) {
                return "해당 날짜의 날씨 정보를 찾을 수 없습니다.";
            } else {
                // 특정 가공 로직을 추가하여 날씨 정보를 표현하거나 반환
                return "날짜 " + dateString + "의 날씨 정보: " + formatWeatherInfo(weatherList.get(0));
            }
        } catch (ParseException e) {
            return "잘못된 날짜 형식입니다.";
        }
    }


    // 경도와 위도로 날씨 조회
    public String getWeatherByCoordinates(double latitude, double longitude) {
        // 경도와 위도를 기반으로 날씨를 조회하는 로직.
        List<Weather> weatherList = weatherRepository.findByLatitudeAndLongitude(latitude, longitude);

        if (weatherList.isEmpty()) {
            return "해당 위치의 날씨 정보를 찾을 수 없습니다.";
        } else {
            // 특정 가공 로직을 추가하여 날씨 정보를 표현하거나 반환.
            return "경도: " + longitude + ", 위도: " + latitude + "의 날씨 정보: " + formatWeatherInfo(weatherList.get(0));
        }
    }

    // 기타 날씨 정보를 가공하는 로직
    private String formatWeatherInfo(Weather weather) {
        // JSON 형식으로 변환하거나 특정 형태로 출력.
        return "현재 날씨 정보: " + weather.getTemperature() + "도, " + weather.getWeatherCondition();
    }
}
