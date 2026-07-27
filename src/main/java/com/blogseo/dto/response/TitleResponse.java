package com.blogseo.dto.response;

import java.util.List;

public record TitleResponse(String keyword, List<TitleDto> titles) {}
