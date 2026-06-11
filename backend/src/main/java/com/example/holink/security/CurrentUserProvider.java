package com.example.holink.security;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CurrentUserProvider {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String DEFAULT_USER_ID = "user_001";

    public String getCurrentUserId() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return DEFAULT_USER_ID;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        String headerValue = request.getHeader(USER_ID_HEADER);
        if (!StringUtils.hasText(headerValue)) {
            return DEFAULT_USER_ID;
        }

        return headerValue.trim();
    }
}
