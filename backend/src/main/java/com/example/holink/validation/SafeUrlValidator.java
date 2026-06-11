package com.example.holink.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.holink.exception.BadRequestException;

@Component
public class SafeUrlValidator {

    private static final int MAX_URL_LENGTH = 2048;
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public String validateRequiredLinkUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new BadRequestException("Link URL is required");
        }

        return validateUrl(url, "Link URL must be a valid http or https URL");
    }

    public String validateOptionalAvatarUrl(String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return null;
        }

        return validateUrl(avatarUrl, "Avatar URL must be a valid http or https URL");
    }

    private String validateUrl(String rawUrl, String invalidMessage) {
        String trimmedUrl = rawUrl.trim();
        if (trimmedUrl.length() > MAX_URL_LENGTH) {
            throw new BadRequestException("URL must not exceed 2048 characters");
        }

        URI uri;
        try {
            uri = new URI(trimmedUrl);
        } catch (URISyntaxException ex) {
            throw new BadRequestException(invalidMessage);
        }

        String scheme = uri.getScheme();
        if (!StringUtils.hasText(scheme)) {
            throw new BadRequestException(invalidMessage);
        }

        String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
        if (!ALLOWED_SCHEMES.contains(normalizedScheme)) {
            throw new BadRequestException(invalidMessage);
        }

        if (!StringUtils.hasText(uri.getHost())) {
            throw new BadRequestException(invalidMessage);
        }

        return trimmedUrl;
    }
}
