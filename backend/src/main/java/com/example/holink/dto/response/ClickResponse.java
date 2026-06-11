package com.example.holink.dto.response;

import lombok.Builder;

@Builder
public record ClickResponse(
        String redirectUrl
) {
}
