package com.example.holink.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.holink.dto.request.ProfileRequest;
import com.example.holink.dto.response.DashboardProfileResponse;
import com.example.holink.dto.response.LinkResponse;
import com.example.holink.dto.response.ProfileResponse;
import com.example.holink.dto.response.PublicProfileResponse;
import com.example.holink.entity.Profile;
import com.example.holink.entity.User;
import com.example.holink.exception.ConflictException;
import com.example.holink.exception.ForbiddenException;
import com.example.holink.exception.NotFoundException;
import com.example.holink.repository.LinkRepository;
import com.example.holink.repository.ProfileRepository;
import com.example.holink.repository.UserRepository;
import com.example.holink.security.CurrentUserProvider;
import com.example.holink.validation.SafeUrlValidator;
import com.example.holink.validation.TextValidator;
import com.example.holink.validation.UsernameNormalizer;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final CurrentUserProvider currentUserProvider;
    private final UsernameNormalizer usernameNormalizer;
    private final TextValidator textValidator;
    private final SafeUrlValidator safeUrlValidator;

    public ProfileService(ProfileRepository profileRepository,
                          UserRepository userRepository,
                          LinkRepository linkRepository,
                          CurrentUserProvider currentUserProvider,
                          UsernameNormalizer usernameNormalizer,
                          TextValidator textValidator,
                          SafeUrlValidator safeUrlValidator) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.linkRepository = linkRepository;
        this.currentUserProvider = currentUserProvider;
        this.usernameNormalizer = usernameNormalizer;
        this.textValidator = textValidator;
        this.safeUrlValidator = safeUrlValidator;
    }

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        User currentUser = getCurrentUser();

        if (profileRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new ConflictException("Current user already has a profile");
        }

        String normalizedUsername = usernameNormalizer.normalize(request.getUsername());
        ensureUsernameAvailable(normalizedUsername, null);

        Profile profile = new Profile();
        profile.setUser(currentUser);
        applyProfileValues(profile, request, normalizedUsername);

        return ProfileResponse.fromEntity(profileRepository.save(profile));
    }

    @Transactional
    public ProfileResponse updateProfile(String profileId, ProfileRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        String currentUserId = currentUserProvider.getCurrentUserId();
        if (!profile.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to update this profile");
        }

        String normalizedUsername = usernameNormalizer.normalize(request.getUsername());
        ensureUsernameAvailable(normalizedUsername, profile.getId());
        applyProfileValues(profile, request, normalizedUsername);

        return ProfileResponse.fromEntity(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public DashboardProfileResponse getDashboardProfile() {
        String currentUserId = currentUserProvider.getCurrentUserId();
        Profile profile = profileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new NotFoundException("Current user does not have a profile"));

        List<LinkResponse> links = linkRepository.findByProfileIdOrderByPositionAsc(profile.getId())
                .stream()
                .map(LinkResponse::fromEntity)
                .toList();

        return DashboardProfileResponse.builder()
                .profile(ProfileResponse.fromEntity(profile))
                .links(links)
                .build();
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(String username) {
        String normalizedUsername = usernameNormalizer.normalize(username);
        Profile profile = profileRepository.findByUsername(normalizedUsername)
                .orElseThrow(() -> new NotFoundException("Public profile not found"));

        List<LinkResponse> links = linkRepository.findByProfileIdAndIsActiveTrueOrderByPositionAsc(profile.getId())
                .stream()
                .map(LinkResponse::fromEntity)
                .toList();

        return PublicProfileResponse.builder()
                .profile(PublicProfileResponse.PublicProfileView.fromEntity(profile))
                .links(links)
                .build();
    }

    private User getCurrentUser() {
        String currentUserId = currentUserProvider.getCurrentUserId();
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Current user not found"));
    }

    private void ensureUsernameAvailable(String username, String currentProfileId) {
        profileRepository.findByUsername(username).ifPresent(existingProfile -> {
            if (currentProfileId == null || !existingProfile.getId().equals(currentProfileId)) {
                throw new ConflictException("Username is already in use");
            }
        });
    }

    private void applyProfileValues(Profile profile, ProfileRequest request, String normalizedUsername) {
        profile.setUsername(normalizedUsername);
        profile.setDisplayName(textValidator.validateDisplayName(request.getDisplayName()));
        profile.setBio(textValidator.validateOptionalBio(request.getBio()));
        profile.setAvatarUrl(safeUrlValidator.validateOptionalAvatarUrl(request.getAvatarUrl()));
    }
}
