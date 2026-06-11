package com.example.holink.dto.response;

import java.util.List;

import com.example.holink.entity.Profile;

import lombok.Builder;

@Builder
public record PublicProfileResponse(
        PublicProfileView profile,
        List<LinkResponse> links
) {

    @Builder
    public record PublicProfileView(
            String username,
            String displayName,
            String bio,
            String avatarUrl
    ) {
        public static PublicProfileView fromEntity(Profile profile) {
            return PublicProfileView.builder()
                    .username(profile.getUsername())
                    .displayName(profile.getDisplayName())
                    .bio(profile.getBio())
                    .avatarUrl(profile.getAvatarUrl())
                    .build();
        }
    }
}
