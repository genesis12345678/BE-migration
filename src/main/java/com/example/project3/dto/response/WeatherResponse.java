package com.example.project3.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {

    @JsonProperty("temperature")
    private double temperature;

    @JsonProperty("weather_description")
    private String weatherDescription;

    @JsonProperty("forecast_date")
    private Date forecastDate;

    @JsonProperty("weather_condition")
    private String weatherCondition;

    @JsonProperty("temperature_max")
    private double temperatureMax;

    @JsonProperty("temperature_min")
    private double temperatureMin;

    @JsonProperty("humidity")
    private int humidity;

    @JsonProperty("wind_speed")
    private double windSpeed;

    @JsonProperty("precipitation")
    private double precipitation;

    @JsonProperty("weather_img")
    private String weatherImg;

    @JsonProperty("observation_time")
    private LocalDateTime observationTime;

    @JsonProperty("location_id")
    private int locationId;

    public WeatherResponse(double temperature, String weatherCondition) {
        this.temperature = temperature;
        this.weatherCondition = weatherCondition;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "temperature=" + temperature +
                ", weatherDescription='" + weatherDescription + '\'' +
                ", forecastDate=" + forecastDate +
                ", weatherCondition='" + weatherCondition + '\'' +
                ", temperatureMax=" + temperatureMax +
                ", temperatureMin=" + temperatureMin +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", precipitation=" + precipitation +
                ", weatherImg='" + weatherImg + '\'' +
                ", observationTime=" + observationTime +
                ", locationId=" + locationId +
                '}';
    }


}
