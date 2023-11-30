package com.example.project3.service.mainApiService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class weatherAPI {

    public static void main(String[] args) throws IOException {
        String serviceKey = "ryCNXqrBb6PzrL17tTbO4MjxBBfAaYVJhtNWxBAzddWaLNQZjWoQKLXMvqXCqpEBz%2BWaB5dSiGrs1cdCrKjeZg%3D%3D"; // 기상청 공공데이터 포털에서 발급받은 API 키
        String apiUrl = "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidFcst"; // 중기예보 API URL

        // API 호출을 위한 URL 생성
        URL url = new URL(apiUrl + "?serviceKey=" + serviceKey + "&pageNo=1&numOfRows=10&dataType=JSON&regId=11B00000&tmFc=202311270600");

        // HTTP 요청
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // 응답 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            System.out.println(response.toString());
        } finally {
            connection.disconnect();
        }
    }
}
