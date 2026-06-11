package com.example.holink.validation;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.example.holink.exception.BadRequestException;

@Component
public class TextValidator {

    private static final int MAX_DISPLAY_NAME_LENGTH = 120;
    private static final int MAX_LINK_TITLE_LENGTH = 120;
    private static final int MAX_BIO_LENGTH = 500;

    public String validateDisplayName(String displayName) {
        return validateRequiredText(displayName, "Display name", MAX_DISPLAY_NAME_LENGTH);
    }

    public String validateLinkTitle(String title) {
        return validateRequiredText(title, "Link title", MAX_LINK_TITLE_LENGTH);
    }

    public String validateOptionalBio(String bio) {
        if (!StringUtils.hasText(bio)) {
            return null;
        }

        String normalizedBio = bio.trim();
        validateScriptLikeInput(normalizedBio, "Bio");
        if (normalizedBio.length() > MAX_BIO_LENGTH) {
            throw new BadRequestException("Bio must not exceed 500 characters");
        }

        return normalizedBio;
    }

    private String validateRequiredText(String value, String fieldName, int maxLength) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required");
        }

        String normalizedValue = value.trim();
        validateScriptLikeInput(normalizedValue, fieldName);
        if (normalizedValue.length() > maxLength) {
            throw new BadRequestException(fieldName + " must not exceed " + maxLength + " characters");
        }

        return normalizedValue;
    }

    private void validateScriptLikeInput(String value, String fieldName) {
        if (value.toLowerCase(Locale.ROOT).contains("<script")) {
            throw new BadRequestException(fieldName + " contains invalid content");
        }
    }
}
