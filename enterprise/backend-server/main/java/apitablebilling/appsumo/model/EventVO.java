package com.apitable.enterprise.apitablebilling.appsumo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Appsumo event VO.
 */
@Data
@Builder(toBuilder = true)
@Schema(description = "Appsumo event vo")
public class EventVO {

    @Schema(description = "message", type = "java.lang.String", example = "***")
    private String message;

    @Schema(description = "redirect url", type = "java.lang.String", example = "***")
    @JsonProperty("redirect_url")
    private String redirectUrl;
}
