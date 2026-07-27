package com.blogseo.dto.response;

public record RelatedKeywordDto(
        String keyword,
        double ratio,
        String popularityLevel
) {}
