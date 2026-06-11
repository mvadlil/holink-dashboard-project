package com.example.holink.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.holink.dto.request.ClickRequest;
import com.example.holink.dto.response.ClickResponse;
import com.example.holink.service.ClickTrackingService;

@RestController
@RequestMapping("/api/links")
public class ClickTrackingController {

    private final ClickTrackingService clickTrackingService;

    public ClickTrackingController(ClickTrackingService clickTrackingService) {
        this.clickTrackingService = clickTrackingService;
    }

    @PostMapping("/{linkId}/click")
    public ClickResponse trackClick(@PathVariable String linkId,
                                    @RequestBody(required = false) ClickRequest request,
                                    @RequestHeader(value = "Referer", required = false) String referrer,
                                    @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        return clickTrackingService.trackClick(linkId, request, referrer, userAgent);
    }
}
