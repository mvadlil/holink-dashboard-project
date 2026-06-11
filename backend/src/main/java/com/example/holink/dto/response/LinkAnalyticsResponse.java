package com.example.holink.dto.response;

import com.example.holink.entity.Link;

import lombok.Builder;

@Builder
public record LinkAnalyticsResponse(
        String linkId,
        String title,
        String url,
        boolean isActive,
        long totalClicks
) {

    public static LinkAnalyticsResponse fromEntity(Link link, long totalClicks) {
        return LinkAnalyticsResponse.builder()
                .linkId(link.getId())
                .title(link.getTitle())
                .url(link.getUrl())
                .isActive(link.isActive())
                .totalClicks(totalClicks)
                .build();
    }
}
