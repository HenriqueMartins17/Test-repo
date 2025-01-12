package com.apitable.enterprise.apitablebilling.appsumo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Appsumo jwt token.
 */
@Data
@Builder(toBuilder = true)
@Schema(description = "Appsumo jwt token vo")
public class TokenVO {

    @Schema(description = "Access token", type = "java.lang.String", example = "***")
    private String access;
}
