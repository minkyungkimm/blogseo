package com.blogseo.dto.naver;

import java.util.List;

public record DataLabResponse(
        String startDate,
        String endDate,
        String timeUnit,
        List<Result> results
) {
    public record Result(String title, List<String> keywords, List<DataPoint> data) {}
    public record DataPoint(String period, double ratio) {}
}
