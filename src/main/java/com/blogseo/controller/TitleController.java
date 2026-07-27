package com.blogseo.controller;

import com.blogseo.dto.request.TitleRequest;
import com.blogseo.dto.response.TitleResponse;
import com.blogseo.service.TitleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/title")
public class TitleController {

    private final TitleService titleService;

    public TitleController(TitleService titleService) {
        this.titleService = titleService;
    }

    @PostMapping("/recommend")
    public ResponseEntity<TitleResponse> recommend(@RequestBody TitleRequest request) {
        List<String> related = request.relatedKeywords() != null
                ? request.relatedKeywords()
                : List.of();
        return ResponseEntity.ok(titleService.recommend(request.keyword(), related));
    }
}
