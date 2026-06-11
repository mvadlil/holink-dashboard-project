package com.example.holink.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.holink.dto.request.CreateLinkRequest;
import com.example.holink.dto.request.UpdateLinkRequest;
import com.example.holink.dto.response.LinkResponse;
import com.example.holink.entity.Link;
import com.example.holink.entity.Profile;
import com.example.holink.exception.BadRequestException;
import com.example.holink.exception.ConflictException;
import com.example.holink.exception.ForbiddenException;
import com.example.holink.exception.NotFoundException;
import com.example.holink.repository.LinkRepository;
import com.example.holink.repository.ProfileRepository;
import com.example.holink.security.CurrentUserProvider;
import com.example.holink.validation.SafeUrlValidator;
import com.example.holink.validation.TextValidator;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUserProvider;
    private final TextValidator textValidator;
    private final SafeUrlValidator safeUrlValidator;

    public LinkService(LinkRepository linkRepository,
                       ProfileRepository profileRepository,
                       CurrentUserProvider currentUserProvider,
                       TextValidator textValidator,
                       SafeUrlValidator safeUrlValidator) {
        this.linkRepository = linkRepository;
        this.profileRepository = profileRepository;
        this.currentUserProvider = currentUserProvider;
        this.textValidator = textValidator;
        this.safeUrlValidator = safeUrlValidator;
    }

    @Transactional
    public LinkResponse createLink(CreateLinkRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        assertOwnership(profile);
        validatePositionForCreate(profile.getId(), request.getPosition());

        Link link = new Link();
        link.setProfile(profile);
        applyLinkValues(link, request.getTitle(), request.getUrl(), request.getIsActive(), request.getPosition());

        return LinkResponse.fromEntity(linkRepository.save(link));
    }

    @Transactional
    public LinkResponse updateLink(String linkId, UpdateLinkRequest request) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new NotFoundException("Link not found"));

        assertOwnership(link.getProfile());
        validatePositionForUpdate(link.getProfile().getId(), request.getPosition(), link.getId());
        applyLinkValues(link, request.getTitle(), request.getUrl(), request.getIsActive(), request.getPosition());

        return LinkResponse.fromEntity(linkRepository.save(link));
    }

    @Transactional
    public void deleteLink(String linkId) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new NotFoundException("Link not found"));

        assertOwnership(link.getProfile());
        linkRepository.delete(link);
    }

    private void assertOwnership(Profile profile) {
        String currentUserId = currentUserProvider.getCurrentUserId();
        if (!profile.getUser().getId().equals(currentUserId)) {
            throw new ForbiddenException("You do not have permission to manage links for this profile");
        }
    }

    private void applyLinkValues(Link link,
                                 String title,
                                 String url,
                                 Boolean isActive,
                                 Integer position) {
        validatePositionValue(position);
        link.setTitle(textValidator.validateLinkTitle(title));
        link.setUrl(safeUrlValidator.validateRequiredLinkUrl(url));
        link.setActive(isActive);
        link.setPosition(position);
    }

    private void validatePositionForCreate(String profileId, Integer position) {
        validatePositionValue(position);
        if (linkRepository.existsByProfileIdAndPosition(profileId, position)) {
            throw new ConflictException("Link position is already used for this profile");
        }
    }

    private void validatePositionForUpdate(String profileId, Integer position, String linkId) {
        validatePositionValue(position);
        if (linkRepository.existsByProfileIdAndPositionAndIdNot(profileId, position, linkId)) {
            throw new ConflictException("Link position is already used for this profile");
        }
    }

    private void validatePositionValue(Integer position) {
        if (position == null || position < 1) {
            throw new BadRequestException("Link position must be at least 1");
        }
    }
}
