package com.blogseo.client;

import com.blogseo.config.NaverApiProperties;
import com.blogseo.dto.naver.DataLabRequest;
import com.blogseo.dto.naver.DataLabResponse;
import com.blogseo.exception.NaverApiException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NaverDataLabClient {

    private final WebClient webClient;
    private final NaverApiProperties properties;

    public NaverDataLabClient(WebClient webClient, NaverApiProperties properties) {
        this.webClient = webClient;
        this.properties = properties;
    }

    public DataLabResponse fetchTrends(DataLabRequest request) {
        return webClient.post()
                .uri(properties.datalabUrl())
                .header("X-Naver-Client-Id", properties.clientId())
                .header("X-Naver-Client-Secret", properties.clientSecret())
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            int status = clientResponse.statusCode().value();
                            return clientResponse.bodyToMono(String.class)
                                    .defaultIfEmpty("(응답 본문 없음)")
                                    .map(body -> new NaverApiException(
                                            "네이버 DataLab API 오류 (HTTP %d): %s".formatted(status, body),
                                            status
                                    ));
                        }
                )
                .bodyToMono(DataLabResponse.class)
                .switchIfEmpty(Mono.error(new NaverApiException("네이버 DataLab API 응답이 비어 있습니다.", 502)))
                .block();
    }
}
