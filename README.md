# SEO 블로그 도우미

네이버 DataLab API를 활용한 키워드 분석 · SEO 제목 생성 · 해시태그 추천 웹서비스

---

## 주요 기능

| 기능 | 설명 |
|------|------|
| **키워드 분석** | 네이버 DataLab API로 최근 12개월 검색 트렌드 조회 |
| **SEO 점수** | 검색량 · 경쟁도 · 추천도 기반 종합 SEO 점수 산출 (0~100점) |
| **트렌드 차트** | 월별 검색 인기도 시각화 (Chart.js) |
| **연관 키워드** | 키워드 파생 검색어 인기도 비교 |
| **SEO 제목 추천** | 카테고리 · 의도 · 연관 키워드 기반 상위 20개 제목 자동 생성 |
| **예상 유입** | 제목별 예상 트래픽 수준 표시 (높음 / 보통 / 낮음) |
| **해시태그 추천** | 네이버 블로그 전용 해시태그 최대 30개 생성 |
| **복사 기능** | 제목 · 해시태그 개별 및 전체 복사 |

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.4 |
| **Build** | Gradle (Kotlin DSL) |
| **View** | Thymeleaf |
| **Frontend** | HTML / CSS / Vanilla JS |
| **HTTP Client** | Spring WebFlux (WebClient) |
| **Chart** | Chart.js 4.4.4 |
| **External API** | 네이버 DataLab API |
| **DB** | 없음 (No DB) |
| **인증** | 없음 (No Login) |

---

## SEO 점수 계산 방식

| 지표 | 가중치 | 설명 |
|------|--------|------|
| 검색량 | 40% | DataLab 최신 인기도 비율 |
| 경쟁도 | 35% | 검색량 · 키워드 길이 기반 추정 (낮을수록 유리) |
| 트렌드 | 25% | 최근 3개월 vs 이전 3개월 증감 비교 |

---

## 제목 생성 카테고리

| 카테고리 | 감지 키워드 예시 | 전용 패턴 예시 |
|----------|-----------------|----------------|
| 음식 | 맛집, 카페, 식당, 디저트 | `혼밥도 가능한 {kw}`, `웨이팅 있어도 갈 만한 이유` |
| 여행 | 여행, 호텔, 펜션, 캠핑 | `2박 3일 완벽 코스`, `당일치기 코스 추천` |
| 건강 | 다이어트, 헬스, 요가, 식단 | `1개월 후기`, `30일 변화 기록` |
| 뷰티 | 뷰티, 스킨케어, 메이크업 | `피부 타입별 총정리`, `피부과 전문의 추천` |
| 자기계발 | 취업, 자격증, 재테크, 코딩 | `독학 가능 후기`, `합격자 핵심 7가지` |
| 쇼핑 | 소품샵, 빈티지, 편집샵 | `감성 투어`, `위치 · 주차 · 가격대 정리` |
| 일반 | (해당 없음) | `BEST 10`, `완벽 가이드`, `트렌드 총정리` |

---

## 프로젝트 구조

```
src/main/java/com/blogseo/
├── config/          # NaverApiProperties, WebClientConfig
├── client/          # NaverDataLabClient (API 호출)
├── controller/      # Keyword / Title / Hashtag / Home
├── service/         # KeywordAnalysisService, TitleService, HashtagService
├── util/            # TitleGenerator, HashtagGenerator (규칙 기반 생성)
├── dto/
│   ├── naver/       # DataLabRequest, DataLabResponse
│   ├── request/     # KeywordAnalysisRequest, TitleRequest, HashtagRequest
│   └── response/    # KeywordAnalysisResponse, TitleDto, SeoScoreDto ...
└── exception/       # GlobalExceptionHandler, NaverApiException
```

---

## API 명세

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/` | 메인 페이지 |
| `POST` | `/api/keyword/analyze` | 키워드 분석 (트렌드 · SEO 점수) |
| `POST` | `/api/title/recommend` | SEO 제목 20개 추천 |
| `POST` | `/api/hashtag/generate` | 해시태그 추천 |

### 요청 · 응답 예시

**POST** `/api/keyword/analyze`
```json
// Request
{ "keyword": "강릉 맛집" }

// Response
{
  "keyword": "강릉 맛집",
  "seoScore": { "score": 78, "stars": 4, "searchVolumeScore": 82, ... },
  "trends": [{ "period": "2025.07", "ratio": 91.0 }, ...],
  "currentRatio": 65.0,
  "trendSummary": "안정적",
  "relatedKeywords": [{ "keyword": "강릉 맛집 추천", "ratio": 12.3 }, ...]
}
```

**POST** `/api/title/recommend`
```json
// Request
{ "keyword": "강릉 맛집", "relatedKeywords": ["강릉 맛집 추천", "강릉 맛집 후기"] }

// Response
{
  "titles": [
    { "title": "강릉 맛집 BEST 10 | 현지인이 직접 추천", "pattern": "숫자형", "trafficLevel": "높음" },
    ...
  ]
}
```

---

## 실행 방법

### 1. 네이버 Open API 키 발급

[네이버 개발자 센터](https://developers.naver.com)에서 애플리케이션 등록 후 **DataLab 검색어 트렌드** 권한 추가

### 2. 환경 설정

`src/main/resources/application.yml` 생성 (`.gitignore` 적용됨)

```yaml
naver:
  api:
    client-id: YOUR_CLIENT_ID
    client-secret: YOUR_CLIENT_SECRET
    datalab-url: https://openapi.naver.com/v1/datalab/search

server:
  port: 8080
```

### 3. 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080` 접속

---

## 주의사항

- `application.yml`은 `.gitignore`에 포함되어 있어 Git에 업로드되지 않습니다
- 네이버 DataLab API는 **무료**이며 일일 호출 한도가 있습니다
- Java 17 이상 필요
