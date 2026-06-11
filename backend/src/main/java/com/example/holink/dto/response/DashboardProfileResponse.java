package com.example.holink.dto.response;

import java.util.List;

import lombok.Builder;

@Builder
public record DashboardProfileResponse(
        ProfileResponse profile,
        List<LinkResponse> links
) {
}
