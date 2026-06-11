package com.example.holink.security;

public record AuthenticatedUser(
        String userId,
        String email,
        String name
) {
}
