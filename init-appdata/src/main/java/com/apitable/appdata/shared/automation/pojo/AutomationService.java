package com.apitable.appdata.shared.automation.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AutomationService {

    private Long id;

    @JsonProperty("service_id")
    private String serviceId;

    private String slug;

    private String name;

    private String description;

    private String logo;

    @JsonProperty("base_url")
    private String baseUrl;

    private String i18n;

    private Long createdBy;

    private Long updatedBy;

}
