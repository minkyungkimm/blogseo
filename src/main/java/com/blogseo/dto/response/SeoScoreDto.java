package com.blogseo.dto.response;

public record SeoScoreDto(
        int score,               // 종합 SEO 점수 0-100
        int stars,               // 종합 별점 1-5

        int searchVolumeScore,   // 검색량 점수 0-100
        String searchVolumeLevel,// 높음/보통/낮음

        int competitionScore,    // 경쟁도 점수 0-100 (낮을수록 좋음)
        String competitionLevel, // 높음/보통/낮음

        int recommendScore,      // 추천도 점수 0-100
        String recommendLevel    // 강력 추천/추천/보통/비추천
) {}
