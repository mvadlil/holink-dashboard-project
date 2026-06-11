package com.example.holink.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.holink.exception.UnauthorizedException;

@Component
public class CurrentUserProvider {

    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication is required");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.userId();
        }
        if (principal instanceof String userId && !userId.isBlank() && !"anonymousUser".equals(userId)) {
            return userId;
        }

        throw new UnauthorizedException("Authentication is required");
    }
}
