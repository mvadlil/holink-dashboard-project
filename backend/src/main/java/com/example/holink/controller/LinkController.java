package com.example.holink.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.holink.dto.request.CreateLinkRequest;
import com.example.holink.dto.request.UpdateLinkRequest;
import com.example.holink.dto.response.LinkResponse;
import com.example.holink.service.LinkService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse createLink(@Valid @RequestBody CreateLinkRequest request) {
        return linkService.createLink(request);
    }

    @PutMapping("/{linkId}")
    public LinkResponse updateLink(@PathVariable String linkId,
                                   @Valid @RequestBody UpdateLinkRequest request) {
        return linkService.updateLink(linkId, request);
    }

    @DeleteMapping("/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLink(@PathVariable String linkId) {
        linkService.deleteLink(linkId);
    }
}
