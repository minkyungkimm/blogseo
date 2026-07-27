package com.blogseo.controller;

import com.blogseo.dto.request.HashtagRequest;
import com.blogseo.dto.response.HashtagResponse;
import com.blogseo.service.HashtagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hashtag")
public class HashtagController {

    private final HashtagService hashtagService;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    @PostMapping("/generate")
    public ResponseEntity<HashtagResponse> generate(@RequestBody HashtagRequest request) {
        return ResponseEntity.ok(hashtagService.generate(request.keyword()));
    }
}
