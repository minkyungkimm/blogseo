package com.blogseo.util;

import com.blogseo.dto.response.HashtagResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class HashtagGenerator {

    private static final int MAX_TAGS = 30;

    private static final Set<String> LOCATIONS = Set.of(
            "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
            "제주", "제주도", "강릉", "속초", "춘천", "여수", "순천", "전주",
            "경주", "안동", "포항", "청주", "천안", "수원", "성남", "고양",
            "용인", "창원", "양양", "가평", "남해", "통영", "울릉도",
            "동해", "묵호", "삼척", "태백", "홍천", "평창", "정선",
            "거제", "사천", "하동", "밀양", "양산", "보성", "고흥", "광양",
            "나주", "담양", "해남", "완도", "서천", "태안", "보령", "당진",
            "공주", "논산", "부여", "아산", "서산", "익산", "군산", "남원"
    );

    private enum Category { FOOD, CAFE, TRAVEL, BEAUTY, FITNESS, BOOK, SHOPPING, GENERAL }

    private static final List<Map.Entry<Category, List<String>>> ORDERED_CATEGORIES = List.of(
            Map.entry(Category.CAFE,     List.of("카페", "커피", "디저트", "케이크", "빵")),
            Map.entry(Category.FOOD,     List.of("맛집", "음식", "식당", "레스토랑", "밥", "점심", "저녁", "브런치", "술집", "횟집", "고깃집", "치킨")),
            Map.entry(Category.TRAVEL,   List.of("여행", "관광", "숙소", "호텔", "펜션", "캠핑", "글램핑")),
            Map.entry(Category.BEAUTY,   List.of("뷰티", "화장품", "스킨케어", "메이크업", "립", "파운데이션", "세럼", "선크림", "헤어", "네일")),
            Map.entry(Category.FITNESS,  List.of("운동", "헬스", "다이어트", "필라테스", "요가", "홈트", "식단")),
            Map.entry(Category.BOOK,     List.of("책", "독서", "소설", "에세이")),
            Map.entry(Category.SHOPPING, List.of("샵", "숍", "소품", "빈티지", "마켓", "시장", "의류", "인테리어", "편집샵", "플리마켓"))
    );

    // 네이버 블로그용 태그만 — *스타그램/*그램/맞팔/소통 완전 제외
    private static final Map<Category, List<String>> CATEGORY_TAGS = Map.of(
            Category.FOOD, List.of(
                    "#맛집추천", "#맛집탐방", "#맛집리뷰", "#맛집후기",
                    "#오늘뭐먹지", "#맛집정보", "#음식리뷰", "#내돈내산후기",
                    "#맛집데이트", "#점심추천", "#저녁추천"
            ),
            Category.CAFE, List.of(
                    "#카페추천", "#카페탐방", "#카페리뷰", "#카페후기",
                    "#커피추천", "#디저트맛집", "#브런치카페",
                    "#분위기좋은카페", "#카페정보", "#카페데이트"
            ),
            Category.TRAVEL, List.of(
                    "#국내여행", "#주말여행", "#여행기록", "#여행일기",
                    "#여행후기", "#국내여행추천", "#당일치기여행",
                    "#여행정보", "#힐링여행", "#주말나들이"
            ),
            Category.BEAUTY, List.of(
                    "#뷰티추천", "#화장품추천", "#뷰티리뷰", "#스킨케어루틴",
                    "#메이크업추천", "#뷰티정보", "#내돈내산화장품",
                    "#화장품리뷰", "#피부관리"
            ),
            Category.FITNESS, List.of(
                    "#운동일기", "#다이어트일기", "#운동루틴",
                    "#홈트레이닝", "#피트니스", "#식단관리",
                    "#건강한생활", "#다이어트식단", "#헬스일상"
            ),
            Category.BOOK, List.of(
                    "#독서기록", "#책추천", "#독서일기", "#오늘의책",
                    "#책리뷰", "#독서노트", "#책읽기", "#추천도서"
            ),
            Category.SHOPPING, List.of(
                    "#쇼핑추천", "#소품샵추천", "#쇼핑리뷰", "#내돈내산",
                    "#쇼핑후기", "#구매후기", "#쇼핑정보", "#아이템추천"
            ),
            Category.GENERAL, List.of(
                    "#정보공유", "#일상기록", "#생활정보", "#솔직후기",
                    "#내돈내산", "#추천템", "#블로그"
            )
    );

    public HashtagResponse generate(String keyword) {
        String[] words    = keyword.trim().split("\\s+");
        String location   = detectLocation(keyword);
        Category category = detectCategory(keyword);
        return new HashtagResponse(keyword, buildTags(keyword, words, location, category));
    }

    private List<String> buildTags(String keyword, String[] words, String location, Category category) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();

        // 1. 키워드 합체  예) #강릉맛집
        tags.add("#" + keyword.replaceAll("\\s+", ""));

        // 2. 각 단어      예) #강릉  #맛집
        for (String w : words) {
            if (!w.isBlank()) tags.add("#" + w);
        }

        // 3. 지역 + 접미사  예) #강릉맛집추천  #강릉여행  #강릉핫플
        if (location != null) {
            for (String suffix : locationSuffixes(category)) {
                tags.add("#" + location + suffix);
                if (tags.size() >= MAX_TAGS) break;
            }
        }

        // 4. 카테고리 태그
        List<String> catTags = CATEGORY_TAGS.getOrDefault(category, CATEGORY_TAGS.get(Category.GENERAL));
        for (String t : catTags) {
            tags.add(t);
            if (tags.size() >= MAX_TAGS) break;
        }

        // 5. 블로그 공통 마무리 태그 (인스타 아닌 네이버 블로그 검색용)
        for (String t : List.of("#일상", "#일상기록", "#주말나들이", "#솔직후기", "#내돈내산")) {
            tags.add(t);
            if (tags.size() >= MAX_TAGS) break;
        }

        List<String> result = new ArrayList<>(tags);
        return result.size() > MAX_TAGS ? result.subList(0, MAX_TAGS) : result;
    }

    private List<String> locationSuffixes(Category category) {
        return switch (category) {
            case FOOD    -> List.of("맛집", "맛집추천", "맛집탐방", "여행", "데이트", "핫플", "여행코스");
            case CAFE    -> List.of("카페", "카페추천", "카페탐방", "여행", "데이트", "핫플");
            case TRAVEL  -> List.of("여행", "여행코스", "핫플", "데이트", "추천", "숙소");
            case SHOPPING-> List.of("쇼핑", "소품샵", "핫플", "데이트코스", "여행");
            default      -> List.of("여행", "데이트", "핫플", "추천", "여행코스");
        };
    }

    private String detectLocation(String keyword) {
        return LOCATIONS.stream().filter(keyword::contains).findFirst().orElse(null);
    }

    private Category detectCategory(String keyword) {
        for (Map.Entry<Category, List<String>> entry : ORDERED_CATEGORIES) {
            if (entry.getValue().stream().anyMatch(keyword::contains)) return entry.getKey();
        }
        return Category.GENERAL;
    }
}
