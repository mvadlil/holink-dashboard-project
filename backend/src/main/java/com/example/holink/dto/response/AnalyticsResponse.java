package com.example.holink.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record AnalyticsResponse(
        List<LinkAnalyticsResponse> items
) {
}
