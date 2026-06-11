package com.example.holink.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.holink.dto.response.AnalyticsResponse;
import com.example.holink.service.AnalyticsService;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/links")
    public AnalyticsResponse getCurrentUserLinkAnalytics() {
        return analyticsService.getCurrentUserLinkAnalytics();
    }
}
