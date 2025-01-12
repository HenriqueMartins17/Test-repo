package com.apitable.appdata.shared.automation.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AutomationActionType {

    private Long id;

    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("action_type_id")
    private String actionTypeId;

    private String name;

    private String description;

    @JsonProperty("input_json_schema")
    private String inputJsonSchema;

    @JsonProperty("output_json_schema")
    private String outputJsonSchema;

    private String endpoint;

    private String i18n;

    private Long createdBy;

    private Long updatedBy;

}
