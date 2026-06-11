package com.example.holink.dto.response;

import com.example.holink.entity.User;

import lombok.Builder;

@Builder
public record UserResponse(
        String id,
        String name,
        String email
) {

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
