package com.example.holink.dto.response;

import com.example.holink.entity.Link;

import lombok.Builder;

@Builder
public record LinkResponse(
        String id,
        String profileId,
        String title,
        String url,
        boolean isActive,
        Integer position
) {

    public static LinkResponse fromEntity(Link link) {
        return LinkResponse.builder()
                .id(link.getId())
                .profileId(link.getProfile().getId())
                .title(link.getTitle())
                .url(link.getUrl())
                .isActive(link.isActive())
                .position(link.getPosition())
                .build();
    }
}
