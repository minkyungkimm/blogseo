package com.blogseo.controller;

import com.blogseo.dto.request.KeywordAnalysisRequest;
import com.blogseo.dto.response.KeywordAnalysisResponse;
import com.blogseo.service.KeywordAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/keyword")
public class KeywordController {

    private final KeywordAnalysisService keywordAnalysisService;

    public KeywordController(KeywordAnalysisService keywordAnalysisService) {
        this.keywordAnalysisService = keywordAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<KeywordAnalysisResponse> analyze(@RequestBody KeywordAnalysisRequest request) {
        KeywordAnalysisResponse response = keywordAnalysisService.analyze(request.keyword());
        return ResponseEntity.ok(response);
    }
}
