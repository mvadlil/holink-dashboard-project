package com.example.holink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.holink.dto.request.ClickRequest;
import com.example.holink.dto.response.ClickResponse;
import com.example.holink.entity.ClickEvent;
import com.example.holink.entity.Link;
import com.example.holink.exception.BadRequestException;
import com.example.holink.exception.NotFoundException;
import com.example.holink.repository.ClickEventRepository;
import com.example.holink.repository.LinkRepository;

@Service
public class ClickTrackingService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackingService.class);

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;

    public ClickTrackingService(LinkRepository linkRepository,
                                ClickEventRepository clickEventRepository) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
    }

    @Transactional
    public ClickResponse trackClick(String linkId, ClickRequest request, String referrer, String userAgent) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new NotFoundException("Link not found"));

        if (!link.isActive()) {
            throw new BadRequestException("Link is inactive");
        }

        try {
            ClickEvent clickEvent = new ClickEvent();
            clickEvent.setLink(link);
            clickEvent.setProfileUsername(link.getProfile().getUsername());
            clickEvent.setUtmSource(request != null ? request.getUtmSource() : null);
            clickEvent.setUtmMedium(request != null ? request.getUtmMedium() : null);
            clickEvent.setUtmCampaign(request != null ? request.getUtmCampaign() : null);
            clickEvent.setReferrer(referrer);
            clickEvent.setUserAgent(userAgent);
            clickEventRepository.save(clickEvent);
        } catch (Exception ex) {
            log.warn("Failed to save click event for link {}", linkId, ex);
        }

        return ClickResponse.builder()
                .redirectUrl(link.getUrl())
                .build();
    }
}
