package com.example.holink.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.holink.dto.request.ProfileRequest;
import com.example.holink.dto.response.DashboardProfileResponse;
import com.example.holink.dto.response.ProfileResponse;
import com.example.holink.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse createProfile(@Valid @RequestBody ProfileRequest request) {
        return profileService.createProfile(request);
    }

    @PutMapping("/profiles/{profileId}")
    public ProfileResponse updateProfile(@PathVariable String profileId,
                                         @Valid @RequestBody ProfileRequest request) {
        return profileService.updateProfile(profileId, request);
    }

    @GetMapping("/me/profile")
    public DashboardProfileResponse getDashboardProfile() {
        return profileService.getDashboardProfile();
    }
}
