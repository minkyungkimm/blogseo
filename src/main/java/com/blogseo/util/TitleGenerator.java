package com.blogseo.util;

import com.blogseo.dto.response.TitleDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class TitleGenerator {

    private static final Set<String> LOCATIONS = Set.of(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
            "제주", "제주도", "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남",
            "수원", "성남", "안양", "고양", "용인", "창원", "청주", "천안", "전주", "포항",
            "강릉", "춘천", "여수", "순천", "목포", "경주", "안동", "진주", "속초", "양양",
            "동해", "묵호", "삼척", "태백", "홍천", "평창", "정선", "영월", "화천", "양구",
            "인제", "고성", "양평", "가평", "파주", "김포", "구리", "남양주", "하남", "의왕",
            "거제", "통영", "사천", "남해", "하동", "함양", "합천", "밀양", "양산",
            "보성", "고흥", "광양", "나주", "담양", "함평", "영암", "해남", "완도", "진도",
            "서천", "태안", "보령", "당진", "공주", "논산", "계룡", "금산", "부여",
            "홍성", "예산", "아산", "서산", "익산", "군산", "김제", "남원", "정읍",
            "문경", "상주", "영주", "영천", "예천", "울진", "울릉", "청도", "칠곡"
    );

    private static final List<String> POWER_WORDS = List.of(
            "추천", "방법", "정리", "가이드", "후기", "분석", "비교", "꿀팁"
    );

    private enum Category { FOOD, TRAVEL, HEALTH, BEAUTY, SELF_DEV, SHOPPING, GENERAL }
    private enum Intent   { HOW_TO, REVIEW, RECOMMENDATION, COST, GENERAL }

    public List<TitleDto> generate(String keyword, List<String> relatedKeywords) {
        int year        = LocalDate.now().getYear();
        String location = detectLocation(keyword);
        String base     = stripLocation(keyword, location);   // 지역 제외 순수 키워드
        Category cat    = detectCategory(keyword);
        Intent intent   = detectIntent(keyword);

        List<TitleDto> candidates = new ArrayList<>();
        candidates.addAll(categoryPatterns(keyword, base, location, year, cat));
        candidates.addAll(intentPatterns(keyword, location, year, intent));
        candidates.addAll(relatedKeywordPatterns(keyword, relatedKeywords, location));
        candidates.addAll(commonPatterns(keyword, year, location));
        candidates.addAll(locationPatterns(keyword, base, year, location));

        // 제목 중복 제거 후 점수 내림차순 상위 20개
        Set<String> seen = new LinkedHashSet<>();
        return candidates.stream()
                .filter(t -> !t.title().isBlank())
                .sorted(Comparator.comparingInt(TitleDto::score).reversed())
                .filter(t -> seen.add(t.title()))
                .limit(20)
                .toList();
    }

    // ── 카테고리별 패턴 ───────────────────────────────────────────────
    private List<TitleDto> categoryPatterns(String kw, String base, String loc, int year, Category cat) {
        return switch (cat) {
            case FOOD     -> foodPatterns(kw, base, loc, year);
            case TRAVEL   -> travelPatterns(kw, base, loc, year);
            case HEALTH   -> healthPatterns(kw, loc, year);
            case BEAUTY   -> beautyPatterns(kw, loc, year);
            case SELF_DEV -> selfDevPatterns(kw, loc, year);
            case SHOPPING -> shoppingPatterns(kw, base, loc, year);
            case GENERAL  -> generalPatterns(kw, loc, year);
        };
    }

    private List<TitleDto> foodPatterns(String kw, String base, String loc, int year) {
        String b = base.isEmpty() ? kw : base;
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " BEST 10 | 현지인이 직접 추천", kw, loc, "숫자형"));
        r.add(make(kw + " TOP 7 | 웨이팅 있어도 갈 만한 이유", kw, loc, "숫자형"));
        r.add(make("혼밥도 가능한 " + kw + " | " + year + "년 최신 업데이트", kw, loc, "팁형"));
        r.add(make("데이트 코스로 딱인 " + kw + " BEST 5", kw, loc, "숫자형"));
        r.add(make(kw + " 솔직 후기 | 직접 먹어봤습니다", kw, loc, "후기형"));
        r.add(make(kw + " 가격 · 메뉴 · 주차 한 번에 정리", kw, loc, "가이드형"));
        r.add(make("SNS에서 핫한 " + kw + " | " + year + "년 인스타 성지", kw, loc, "팁형"));
        r.add(make(kw + " 예약 방법부터 주문 팁까지 총정리", kw, loc, "가이드형"));
        r.add(make("내돈내산 " + kw + " 리뷰 | 재방문 의사 있나요?", kw, loc, "후기형"));
        if (loc != null) {
            r.add(make(loc + " 가면 꼭 가야 할 " + b + " 7곳", kw, loc, "지역형"));
            r.add(make(loc + " 여행 필수 " + b + " | 현지인 추천 리스트", kw, loc, "지역형"));
        }
        return r;
    }

    private List<TitleDto> travelPatterns(String kw, String base, String loc, int year) {
        String b = base.isEmpty() ? kw : base;
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " 2박 3일 완벽 코스 | 이 루트로 가세요", kw, loc, "가이드형"));
        r.add(make(kw + " 준비물 · 경비 · 일정 총정리", kw, loc, "가이드형"));
        r.add(make(kw + " 혼자 가도 좋은 이유 5가지", kw, loc, "팁형"));
        r.add(make(year + "년 " + kw + " | 이것만 챙기면 됩니다", kw, loc, "연도형"));
        r.add(make(kw + " 숙소 · 맛집 · 명소 한 번에 정리", kw, loc, "가이드형"));
        r.add(make(kw + " 실제 비용 공개 | 예산 얼마면 될까요", kw, loc, "팁형"));
        r.add(make(kw + " 처음이라면 꼭 알아야 할 7가지", kw, loc, "팁형"));
        r.add(make("당일치기 " + kw + " 완벽 코스 추천", kw, loc, "가이드형"));
        r.add(make(kw + " 포토스팟 BEST 10 | 인생샷 찍는 법", kw, loc, "숫자형"));
        if (loc != null) {
            r.add(make(loc + " 여행 일정 3박 4일 | 알차게 짜는 법", kw, loc, "지역형"));
            r.add(make(loc + " " + year + "년 여행 트렌드 | 지금 뜨는 곳", kw, loc, "지역형"));
        }
        return r;
    }

    private List<TitleDto> healthPatterns(String kw, String loc, int year) {
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " 1개월 후기 | 실제로 효과 있었나요?", kw, loc, "후기형"));
        r.add(make(kw + " 올바른 방법 | 전문가가 알려드립니다", kw, loc, "방법형"));
        r.add(make(kw + " 전 꼭 알아야 할 주의사항 5가지", kw, loc, "팁형"));
        r.add(make(kw + " 효과 없는 이유 TOP 3 | 이렇게 해보세요", kw, loc, "팁형"));
        r.add(make(year + "년 " + kw + " 완벽 가이드 | 검증된 방법만", kw, loc, "연도형"));
        r.add(make("전문가가 알려주는 " + kw + " 핵심 7가지", kw, loc, "전문가형"));
        r.add(make(kw + " 직접 해봤습니다 | 30일 변화 기록", kw, loc, "후기형"));
        r.add(make(kw + " 잘못 알고 있는 상식 5가지", kw, loc, "팁형"));
        r.add(make("하루 30분 " + kw + " | 한 달이면 달라집니다", kw, loc, "팁형"));
        r.add(make(kw + " BEST 7 | 전문가 추천 루틴 공개", kw, loc, "숫자형"));
        return r;
    }

    private List<TitleDto> beautyPatterns(String kw, String loc, int year) {
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " 직접 써봤습니다 | 한 달 사용 후기", kw, loc, "후기형"));
        r.add(make(kw + " 추천 BEST 5 | 피부 타입별 총정리", kw, loc, "숫자형"));
        r.add(make(kw + " 올바른 순서와 방법 | 이렇게 해야 효과 있어요", kw, loc, "방법형"));
        r.add(make(year + "년 " + kw + " 트렌드 | 지금 이게 대세입니다", kw, loc, "연도형"));
        r.add(make(kw + " 가성비 TOP 7 | 가격대별 추천", kw, loc, "숫자형"));
        r.add(make(kw + " 잘못 알고 있는 5가지 상식", kw, loc, "팁형"));
        r.add(make("피부과 전문의가 추천하는 " + kw + " | 검증 완료", kw, loc, "전문가형"));
        r.add(make("내돈내산 " + kw + " 리뷰 | 재구매 의사 있나요?", kw, loc, "후기형"));
        r.add(make(kw + " 입문자 추천 | 이것만 있으면 됩니다", kw, loc, "가이드형"));
        r.add(make(kw + " 전후 비교 | 직접 사용해봤습니다", kw, loc, "후기형"));
        return r;
    }

    private List<TitleDto> selfDevPatterns(String kw, String loc, int year) {
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " 독학으로 가능할까요? | 현실 후기", kw, loc, "후기형"));
        r.add(make(kw + " 3개월 도전기 | 솔직하게 알려드립니다", kw, loc, "후기형"));
        r.add(make(kw + " 시작하는 방법 | 완벽 로드맵", kw, loc, "방법형"));
        r.add(make(year + "년 " + kw + " 로드맵 | 이 순서로 하세요", kw, loc, "연도형"));
        r.add(make(kw + " 추천 교재 · 방법 · 일정 총정리", kw, loc, "가이드형"));
        r.add(make(kw + " 합격자가 알려주는 핵심 7가지", kw, loc, "전문가형"));
        r.add(make(kw + " 하기 전 반드시 읽어보세요", kw, loc, "팁형"));
        r.add(make(kw + " 효율을 높이는 5가지 방법", kw, loc, "팁형"));
        r.add(make("직장인도 할 수 있는 " + kw + " | 시간 관리 포함", kw, loc, "팁형"));
        r.add(make(kw + " 합격 비결 TOP 5 | 이것만 따라하세요", kw, loc, "숫자형"));
        return r;
    }

    private List<TitleDto> shoppingPatterns(String kw, String base, String loc, int year) {
        String b = base.isEmpty() ? kw : base;
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " BEST 7 | 감성 넘치는 곳만 골랐습니다", kw, loc, "숫자형"));
        r.add(make(kw + " 투어 | 이 거리 다 돌았습니다", kw, loc, "후기형"));
        r.add(make(kw + " 추천 | 선물 고르기 딱 좋은 곳", kw, loc, "팁형"));
        r.add(make("감성 넘치는 " + kw + " | 인스타 성지 모음", kw, loc, "팁형"));
        r.add(make(kw + " 직접 다녀왔습니다 | 솔직 방문 후기", kw, loc, "후기형"));
        r.add(make(kw + " 위치 · 주차 · 가격대 한 번에 정리", kw, loc, "가이드형"));
        r.add(make(kw + " | " + year + "년 새로 생긴 곳 포함 최신 업데이트", kw, loc, "연도형"));
        r.add(make("SNS에서 핫한 " + kw + " | 직접 가봤습니다", kw, loc, "팁형"));
        r.add(make(kw + " 구경하기 좋은 5곳 | 당일치기 코스", kw, loc, "숫자형"));
        if (loc != null) {
            r.add(make(loc + " 가면 꼭 들러야 할 " + b + " | 추천 목록", kw, loc, "지역형"));
            r.add(make(loc + " " + b + " 모음 | " + year + "년 최신 정보", kw, loc, "지역형"));
        }
        return r;
    }

    private List<TitleDto> generalPatterns(String kw, String loc, int year) {
        List<TitleDto> r = new ArrayList<>();
        r.add(make(kw + " BEST 10 | 지금 바로 확인하세요", kw, loc, "숫자형"));
        r.add(make(kw + " TOP 7 완벽 정리", kw, loc, "숫자형"));
        r.add(make(kw + " 추천 5가지 | " + year + "년 최신 정보", kw, loc, "숫자형"));
        r.add(make(kw + " 직접 경험해봤습니다 | 솔직 후기", kw, loc, "후기형"));
        r.add(make(kw + " 모르면 손해 | 핵심 정보 총정리", kw, loc, "팁형"));
        r.add(make(kw + " 꼭 알아야 할 5가지", kw, loc, "팁형"));
        r.add(make("전문가가 추천하는 " + kw + " BEST 7", kw, loc, "전문가형"));
        r.add(make(kw + " 완벽 가이드 | " + year + "년 최신 업데이트", kw, loc, "가이드형"));
        r.add(make(kw + " 총정리 | 이 글 하나로 끝내세요", kw, loc, "가이드형"));
        r.add(make(year + "년 " + kw + " 트렌드 | 지금 이게 대세", kw, loc, "연도형"));
        return r;
    }

    // ── 의도별 패턴 ───────────────────────────────────────────────────
    private List<TitleDto> intentPatterns(String kw, String loc, int year, Intent intent) {
        return switch (intent) {
            case HOW_TO -> List.of(
                    make(kw + " 3단계로 쉽게 끝내는 법", kw, loc, "방법형"),
                    make(kw + " 초보자도 따라할 수 있는 방법 총정리", kw, loc, "방법형"),
                    make("전문가처럼 하는 " + kw + " 비법 공개", kw, loc, "전문가형")
            );
            case REVIEW -> List.of(
                    make(kw + " 장단점 솔직 공개 | 직접 해봤습니다", kw, loc, "후기형"),
                    make("내돈내산 " + kw + " | 솔직하게 말씀드립니다", kw, loc, "후기형"),
                    make(kw + " 1년 사용기 | 재구매 의사 있나요?", kw, loc, "후기형")
            );
            case RECOMMENDATION -> List.of(
                    make(kw + " 전문가 PICK | 검증된 것만 모았습니다", kw, loc, "전문가형"),
                    make("후회 없는 " + kw + " 선택 기준 7가지", kw, loc, "팁형"),
                    make(kw + " " + year + "년 최신 업데이트 | 지금 이게 최고", kw, loc, "연도형")
            );
            case COST -> List.of(
                    make(kw + " 실제 비용 공개 | 얼마나 들까요?", kw, loc, "팁형"),
                    make(kw + " 저렴하게 하는 방법 | 절약 꿀팁 5가지", kw, loc, "팁형"),
                    make(kw + " 가격 비교 · 추천 총정리", kw, loc, "비교형")
            );
            case GENERAL -> List.of(
                    make(kw + " 고르는 방법 | 이것만 보면 됩니다", kw, loc, "방법형"),
                    make(kw + " 추천 5가지 | " + year + "년 최신 업데이트", kw, loc, "숫자형"),
                    make(kw + " 핵심만 모았습니다 | 딱 5분이면 끝", kw, loc, "팁형")
            );
        };
    }

    // ── 연관 키워드 활용 패턴 ─────────────────────────────────────────
    private List<TitleDto> relatedKeywordPatterns(String baseKw, List<String> relatedKeywords, String loc) {
        if (relatedKeywords == null || relatedKeywords.isEmpty()) return List.of();
        List<TitleDto> r = new ArrayList<>();
        for (String rk : relatedKeywords.stream().limit(4).toList()) {
            if (rk.isBlank() || rk.equals(baseKw)) continue;
            // 연관 키워드를 제목 앞에 배치 → 해당 세부 키워드로도 검색 유입 가능
            r.add(make(rk + " BEST 10 | 직접 경험하고 추천합니다", baseKw, loc, "연관형"));
            r.add(make(rk + " 총정리 | 이 글 하나로 해결하세요", baseKw, loc, "연관형"));
        }
        return r;
    }

    // ── 공통 패턴 ─────────────────────────────────────────────────────
    private List<TitleDto> commonPatterns(String kw, int year, String loc) {
        return List.of(
                make(year + "년 " + kw + " 완벽 가이드 | 이것만 보세요", kw, loc, "연도형"),
                make(kw + " 이 글 하나로 끝내세요", kw, loc, "가이드형"),
                make(kw + " 완벽 정리 | 초보자부터 고수까지", kw, loc, "가이드형"),
                make(kw + " 장단점 비교 | 이것만 알면 됩니다", kw, loc, "비교형"),
                make("초보자를 위한 " + kw + " 완벽 가이드 10가지", kw, loc, "가이드형"),
                make(kw + " 써보고 알게 된 5가지", kw, loc, "후기형")
        );
    }

    // ── 지역 패턴 ─────────────────────────────────────────────────────
    private List<TitleDto> locationPatterns(String kw, String base, int year, String loc) {
        if (loc == null) return List.of();
        String b = base.isEmpty() ? kw : base;

        if (kw.startsWith(loc)) {
            return List.of(
                    make(kw + " 현지인 추천 BEST 10", kw, loc, "지역형"),
                    make(kw + " 현지인만 아는 7가지 꿀팁", kw, loc, "지역형"),
                    make("현지인이 추천하는 " + kw + " | " + year + "년 버전", kw, loc, "지역형")
            );
        } else {
            return List.of(
                    make(loc + " " + b + " BEST 10 | 현지인 추천", kw, loc, "지역형"),
                    make(loc + "에서 꼭 해야 할 " + b + " 7가지", kw, loc, "지역형"),
                    make(loc + " " + b + " 완벽 정리 | " + year + "년 업데이트", kw, loc, "지역형")
            );
        }
    }

    // ── 점수 계산 ─────────────────────────────────────────────────────
    private TitleDto make(String title, String keyword, String location, String pattern) {
        int s = score(title, keyword, location);
        String trafficLevel = s >= 8 ? "높음" : s >= 6 ? "보통" : "낮음";
        return new TitleDto(title, s, pattern, trafficLevel);
    }

    private int score(String title, String keyword, String location) {
        int s = 0;
        if (title.startsWith(keyword))    s += 3;
        else if (title.contains(keyword)) s += 1;
        if (title.matches(".*\\d+.*"))    s += 2;
        int len = title.length();
        if (len >= 20 && len <= 40)       s += 2;
        else if (len >= 15 && len < 20)   s += 1;
        if (location != null && title.contains(location)) s += 1;
        if (POWER_WORDS.stream().anyMatch(title::contains)) s += 1;
        return s;
    }

    // ── 카테고리 감지 ─────────────────────────────────────────────────
    private Category detectCategory(String keyword) {
        if (containsAny(keyword, "맛집", "카페", "식당", "음식", "요리", "레스토랑", "빵", "커피", "디저트", "술집", "횟집", "고깃집", "한식", "양식", "분식", "치킨")) return Category.FOOD;
        if (containsAny(keyword, "여행", "투어", "관광", "숙소", "호텔", "펜션", "게스트하우스", "캠핑", "글램핑", "리조트")) return Category.TRAVEL;
        if (containsAny(keyword, "다이어트", "운동", "헬스", "홈트", "요가", "필라테스", "건강", "영양제", "보충제", "식단", "체중", "근력", "유산소")) return Category.HEALTH;
        if (containsAny(keyword, "화장", "뷰티", "피부", "헤어", "네일", "메이크업", "스킨케어", "로션", "세럼", "에센스", "선크림", "마스크팩")) return Category.BEAUTY;
        if (containsAny(keyword, "취업", "자격증", "공부", "독서", "습관", "영어", "코딩", "투자", "재테크", "부업", "창업", "면접", "이직", "토익")) return Category.SELF_DEV;
        if (containsAny(keyword, "샵", "숍", "shop", "쇼핑", "편집샵", "소품", "빈티지", "마켓", "시장", "플리마켓", "인테리어", "소품샵", "의류", "옷가게", "잡화")) return Category.SHOPPING;
        return Category.GENERAL;
    }

    // ── 의도 감지 ─────────────────────────────────────────────────────
    private Intent detectIntent(String keyword) {
        if (containsAny(keyword, "방법", "하는법", "하는방법", "단계")) return Intent.HOW_TO;
        if (containsAny(keyword, "후기", "리뷰", "사용기", "내돈내산")) return Intent.REVIEW;
        if (containsAny(keyword, "추천", "베스트", "BEST", "TOP", "best", "top")) return Intent.RECOMMENDATION;
        if (containsAny(keyword, "비용", "가격", "얼마", "저렴")) return Intent.COST;
        return Intent.GENERAL;
    }

    // ── 지역명 추출 ───────────────────────────────────────────────────
    private String detectLocation(String keyword) {
        return LOCATIONS.stream().filter(keyword::contains).findFirst().orElse(null);
    }

    // ── 키워드에서 지역 제거 ──────────────────────────────────────────
    private String stripLocation(String keyword, String location) {
        if (location == null) return keyword;
        return keyword.replace(location, "").trim();
    }

    private boolean containsAny(String text, String... words) {
        for (String w : words) if (text.contains(w)) return true;
        return false;
    }
}
