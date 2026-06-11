package com.example.holink.dto.response;

import com.example.holink.entity.Profile;

import lombok.Builder;

@Builder
public record ProfileResponse(
        String id,
        String username,
        String displayName,
        String bio,
        String avatarUrl
) {

    public static ProfileResponse fromEntity(Profile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .username(profile.getUsername())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }
}
