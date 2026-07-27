package com.blogseo.service;

import com.blogseo.dto.response.TitleResponse;
import com.blogseo.util.TitleGenerator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TitleService {

    private final TitleGenerator titleGenerator;

    public TitleService(TitleGenerator titleGenerator) {
        this.titleGenerator = titleGenerator;
    }

    public TitleResponse recommend(String keyword, List<String> relatedKeywords) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("키워드를 입력해 주세요.");
        }
        return new TitleResponse(keyword, titleGenerator.generate(keyword, relatedKeywords));
    }
}
