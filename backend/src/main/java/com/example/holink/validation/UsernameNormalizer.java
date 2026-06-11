package com.example.holink.validation;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.holink.exception.BadRequestException;

@Component
public class UsernameNormalizer {

    private static final Pattern ALLOWED_PATTERN = Pattern.compile("^[a-z0-9_-]{3,30}$");

    public String normalize(String input) {
        if (input == null) {
            throw new BadRequestException("Username is required");
        }

        String normalized = input.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "");

        if (!ALLOWED_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("Username must be 3-30 characters and contain only a-z, 0-9, underscore, or hyphen");
        }

        return normalized;
    }
}
