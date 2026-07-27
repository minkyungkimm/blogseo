package com.blogseo.dto.request;

import java.util.List;

public record TitleRequest(String keyword, List<String> relatedKeywords) {}
