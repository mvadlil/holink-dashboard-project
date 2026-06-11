package com.example.holink.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "displayName is required")
    private String displayName;

    private String bio;

    private String avatarUrl;
}
