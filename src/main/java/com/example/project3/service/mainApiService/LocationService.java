package com.example.project3.service.mainApiService;

import com.example.project3.dto.response.LocationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

//TODO: 위치 에 대해서 좀더 정확히 나오면 빨리 만들기
@Service
public class LocationService {

    private final String LOCATION_API_BASE_URL = "https://api.example.com/location"; // 여러분이 사용하는 API의 엔드포인트로 변경해야 합니다.

    private final RestTemplate restTemplate;

    public LocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    // 위치 정보를 가져오는 메서드
    public Mono<LocationResponse> getLocationByCoordinates(double latitude, double longitude) {
        // 가상의 비동기 작업을 수행하고 Mono로 결과를 감싸 반환
        return Mono.fromCallable(() -> {
            // 실제로는 여기에서 외부 서비스나 데이터베이스를 조회하여 LocationResponse를 얻어와야 함
            LocationResponse locationResponse = new LocationResponse();
            locationResponse.setLocationId(123);
            locationResponse.setLatitude(66.7);
            locationResponse.setLongitude(35.7);
            return locationResponse;
        });
    }

    // 여러분이 사용하는 API의 경로를 반영하여 실제로 호출해야 하는 URL로 변환하는 메서드
    private String buildLocationApiUrl(double latitude, double longitude) {
        return LOCATION_API_BASE_URL + "/location?latitude=" + latitude + "&longitude=" + longitude;
    }
}

