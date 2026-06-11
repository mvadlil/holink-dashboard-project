package com.example.holink.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.holink.dto.response.AnalyticsResponse;
import com.example.holink.dto.response.LinkAnalyticsResponse;
import com.example.holink.entity.Link;
import com.example.holink.entity.Profile;
import com.example.holink.exception.NotFoundException;
import com.example.holink.repository.ClickEventRepository;
import com.example.holink.repository.LinkClickCountView;
import com.example.holink.repository.LinkRepository;
import com.example.holink.repository.ProfileRepository;
import com.example.holink.security.CurrentUserProvider;

@Service
public class AnalyticsService {

    private final CurrentUserProvider currentUserProvider;
    private final ProfileRepository profileRepository;
    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;

    public AnalyticsService(CurrentUserProvider currentUserProvider,
                            ProfileRepository profileRepository,
                            LinkRepository linkRepository,
                            ClickEventRepository clickEventRepository) {
        this.currentUserProvider = currentUserProvider;
        this.profileRepository = profileRepository;
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse getCurrentUserLinkAnalytics() {
        String currentUserId = currentUserProvider.getCurrentUserId();
        Profile profile = profileRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new NotFoundException("Current user does not have a profile"));

        List<Link> links = linkRepository.findByProfileIdOrderByPositionAsc(profile.getId());
        if (links.isEmpty()) {
            return AnalyticsResponse.builder()
                    .items(List.of())
                    .build();
        }

        Map<String, Long> clickCountsByLinkId = clickEventRepository.countClicksGroupedByLinkIds(
                        links.stream().map(Link::getId).toList())
                .stream()
                .collect(Collectors.toMap(LinkClickCountView::getLinkId, LinkClickCountView::getTotalClicks));

        List<LinkAnalyticsResponse> items = links.stream()
                .map(link -> LinkAnalyticsResponse.fromEntity(link, clickCountsByLinkId.getOrDefault(link.getId(), 0L)))
                .toList();

        return AnalyticsResponse.builder()
                .items(items)
                .build();
    }
}
