package com.blogseo.service;

import com.blogseo.dto.response.HashtagResponse;
import com.blogseo.util.HashtagGenerator;
import org.springframework.stereotype.Service;

@Service
public class HashtagService {

    private final HashtagGenerator hashtagGenerator;

    public HashtagService(HashtagGenerator hashtagGenerator) {
        this.hashtagGenerator = hashtagGenerator;
    }

    public HashtagResponse generate(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("키워드를 입력해 주세요.");
        }
        return hashtagGenerator.generate(keyword.trim());
    }
}
