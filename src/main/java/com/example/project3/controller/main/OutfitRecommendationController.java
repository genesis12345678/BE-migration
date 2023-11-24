package com.example.project3.controller.main;

import com.example.project3.service.mainApiService.OutfitRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class OutfitRecommendationController {

    private final OutfitRecommendationService outfitRecommendationService;

    @Autowired
    public OutfitRecommendationController(OutfitRecommendationService recommendationService) {
        this.outfitRecommendationService = recommendationService;
    }

    // 날씨에 따라 옷 추천하는 엔드포인트
    @GetMapping("/recommendation/{weather}")
    public String recommendClothes(@PathVariable String weather) {
        return outfitRecommendationService.recommendClothesByWeather(weather);
    }
}
