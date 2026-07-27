package com.blogseo.dto.response;

import java.util.List;

public record KeywordAnalysisResponse(
        String keyword,
        SeoScoreDto seoScore,
        List<TrendPointDto> trends,
        double currentRatio,
        double peakRatio,
        String peakPeriod,
        String trendSummary,
        String popularityLevel,
        List<RelatedKeywordDto> relatedKeywords
) {}
