package com.example.holink.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.holink.dto.response.PublicProfileResponse;
import com.example.holink.service.ProfileService;

@RestController
@RequestMapping("/api/public")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{username}")
    public PublicProfileResponse getPublicProfile(@PathVariable String username) {
        return profileService.getPublicProfile(username);
    }
}
