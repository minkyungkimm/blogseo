package com.blogseo.service;

import com.blogseo.client.NaverDataLabClient;
import com.blogseo.dto.naver.DataLabRequest;
import com.blogseo.dto.naver.DataLabRequest.KeywordGroup;
import com.blogseo.dto.naver.DataLabResponse;
import com.blogseo.dto.naver.DataLabResponse.DataPoint;
import com.blogseo.dto.naver.DataLabResponse.Result;
import com.blogseo.dto.response.KeywordAnalysisResponse;
import com.blogseo.dto.response.RelatedKeywordDto;
import com.blogseo.dto.response.SeoScoreDto;
import com.blogseo.dto.response.TrendPointDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class KeywordAnalysisService {

    private static final DateTimeFormatter API_FORMAT     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM");

    // DataLab 1회 요청에 최대 5그룹 → 기본 키워드 + 4가지 접미사 변형
    private static final List<String> SUFFIXES = List.of(" 추천", " 방법", " 후기", " 비용");

    private final NaverDataLabClient dataLabClient;

    public KeywordAnalysisService(NaverDataLabClient dataLabClient) {
        this.dataLabClient = dataLabClient;
    }

    public KeywordAnalysisResponse analyze(String keyword) {
        validate(keyword);

        LocalDate endDate   = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(12).withDayOfMonth(1);

        List<KeywordGroup> groups = buildGroups(keyword);

        DataLabRequest request = new DataLabRequest(
                startDate.format(API_FORMAT),
                endDate.format(API_FORMAT),
                "month",
                groups
        );

        DataLabResponse response = dataLabClient.fetchTrends(request);
        return toResponse(keyword, response);
    }

    // ── 키워드 그룹 구성 ──────────────────────────────────────────────
    private List<KeywordGroup> buildGroups(String keyword) {
        var groups = new java.util.ArrayList<KeywordGroup>();
        groups.add(new KeywordGroup(keyword, List.of(keyword)));
        SUFFIXES.forEach(suffix ->
                groups.add(new KeywordGroup(keyword + suffix, List.of(keyword + suffix)))
        );
        return groups;
    }

    // ── 응답 → DTO 변환 ───────────────────────────────────────────────
    private KeywordAnalysisResponse toResponse(String keyword, DataLabResponse response) {
        if (response.results() == null || response.results().isEmpty()) {
            throw new IllegalStateException("키워드 분석 결과가 없습니다.");
        }

        // results[0] = 기본 키워드 트렌드
        List<TrendPointDto> trends = response.results().get(0).data().stream()
                .map(d -> new TrendPointDto(formatPeriod(d.period()), round(d.ratio())))
                .toList();

        // 데이터 없음 → 사용자에게 의미있는 오류 반환
        if (trends.isEmpty()) {
            throw new IllegalArgumentException(
                    "'" + keyword + "'에 대한 검색 데이터가 없습니다. 더 일반적인 키워드로 입력해 보세요.");
        }

        double currentRatio = trends.get(trends.size() - 1).ratio();

        TrendPointDto peak = trends.stream()
                .max(Comparator.comparingDouble(TrendPointDto::ratio))
                .orElseThrow();

        // results[1~4] = 연관 키워드 (3개월 평균 ratio로 인기도 측정)
        List<RelatedKeywordDto> relatedKeywords = response.results().stream()
                .skip(1)
                .map(this::toRelatedKeyword)
                .filter(r -> r.ratio() > 0)
                .sorted(Comparator.comparingDouble(RelatedKeywordDto::ratio).reversed())
                .toList();

        String trendSummary = calcTrendSummary(trends);
        SeoScoreDto seoScore = calcSeoScore(keyword, currentRatio, trendSummary);

        return new KeywordAnalysisResponse(
                keyword,
                seoScore,
                trends,
                round(currentRatio),
                round(peak.ratio()),
                peak.period(),
                trendSummary,
                calcPopularityLevel(currentRatio),
                relatedKeywords
        );
    }

    private RelatedKeywordDto toRelatedKeyword(Result result) {
        List<DataPoint> data = result.data();
        // 최근 3개월 평균으로 인기도 측정
        int fromIndex = Math.max(0, data.size() - 3);
        double avgRatio = data.subList(fromIndex, data.size()).stream()
                .mapToDouble(DataPoint::ratio)
                .average()
                .orElse(0);
        double ratio = round(avgRatio);
        return new RelatedKeywordDto(result.title(), ratio, calcPopularityLevel(ratio));
    }

    // ── 트렌드 방향 ───────────────────────────────────────────────────
    private String calcTrendSummary(List<TrendPointDto> trends) {
        if (trends.size() < 6) return "데이터 부족";
        double recent = average(trends.subList(trends.size() - 3, trends.size()));
        double prev   = average(trends.subList(trends.size() - 6, trends.size() - 3));
        if (prev == 0) return "안정적";
        if (recent > prev * 1.1) return "상승세";
        if (recent < prev * 0.9) return "하락세";
        return "안정적";
    }

    // ── SEO 점수 계산 ─────────────────────────────────────────────────
    private SeoScoreDto calcSeoScore(String keyword, double currentRatio, String trendSummary) {
        // 검색량 점수 (0-100)
        int searchScore = (int) currentRatio;

        // 경쟁도 추정: 검색량 높을수록, 단어 짧을수록 경쟁 심함
        int wordCount = keyword.trim().split("\\s+").length;
        int competitionAdj = switch (wordCount) {
            case 1  ->  15;   // 단어 1개 → 경쟁 높음
            case 2  ->   5;   // 단어 2개 → 보통
            default -> -10;   // 3개 이상 롱테일 → 경쟁 낮음
        };
        int competitionScore = (int) Math.min(100, Math.max(0, currentRatio * 0.7 + competitionAdj));

        // 트렌드 점수
        int trendScore = switch (trendSummary) {
            case "상승세"  -> 100;
            case "안정적"  ->  70;
            case "하락세"  ->  30;
            default        ->  50;
        };

        // 추천도: 검색 많고 경쟁 낮고 상승세일수록 높음
        int recommendScore = (int) Math.min(100, searchScore * 0.4 + (100 - competitionScore) * 0.4 + trendScore * 0.2);

        // 종합 SEO 점수
        int seoScore = (int) Math.min(100, searchScore * 0.4 + (100 - competitionScore) * 0.35 + trendScore * 0.25);

        return new SeoScoreDto(
                seoScore,            toStars(seoScore),
                searchScore,         toLevel(searchScore),
                competitionScore,    toCompetitionLevel(competitionScore),
                recommendScore,      toRecommendLevel(recommendScore)
        );
    }

    private int toStars(int score) {
        if (score >= 80) return 5;
        if (score >= 60) return 4;
        if (score >= 40) return 3;
        if (score >= 20) return 2;
        return 1;
    }

    private String toLevel(int score) {
        if (score >= 70) return "높음";
        if (score >= 40) return "보통";
        return "낮음";
    }

    private String toCompetitionLevel(int score) {
        if (score >= 65) return "높음";
        if (score >= 35) return "보통";
        return "낮음";
    }

    private String toRecommendLevel(int score) {
        if (score >= 70) return "강력 추천";
        if (score >= 50) return "추천";
        if (score >= 35) return "보통";
        return "비추천";
    }

    // ── 인기도 레벨 ───────────────────────────────────────────────────
    private String calcPopularityLevel(double ratio) {
        if (ratio >= 80) return "매우 높음";
        if (ratio >= 60) return "높음";
        if (ratio >= 40) return "보통";
        if (ratio >= 20) return "낮음";
        return "매우 낮음";
    }

    // ── 유틸 ──────────────────────────────────────────────────────────
    private double average(List<TrendPointDto> list) {
        return list.stream().mapToDouble(TrendPointDto::ratio).average().orElse(0);
    }

    private String formatPeriod(String period) {
        return LocalDate.parse(period, API_FORMAT).format(DISPLAY_FORMAT);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private void validate(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("키워드를 입력해 주세요.");
        }
        if (keyword.length() > 50) {
            throw new IllegalArgumentException("키워드는 50자 이내로 입력해 주세요.");
        }
    }
}
