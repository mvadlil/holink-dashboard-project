package com.example.holink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateLinkRequest {

    @NotBlank(message = "profileId is required")
    private String profileId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "url is required")
    private String url;

    @NotNull(message = "isActive is required")
    private Boolean isActive;

    @NotNull(message = "position is required")
    private Integer position;
}
