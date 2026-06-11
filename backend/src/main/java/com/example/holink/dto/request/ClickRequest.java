package com.example.holink.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClickRequest {

    private String utmSource;

    private String utmMedium;

    private String utmCampaign;
}
