package com.blogseo.dto.naver;

import java.util.List;

public record DataLabRequest(
        String startDate,
        String endDate,
        String timeUnit,
        List<KeywordGroup> keywordGroups
) {
    public record KeywordGroup(String groupName, List<String> keywords) {}
}
