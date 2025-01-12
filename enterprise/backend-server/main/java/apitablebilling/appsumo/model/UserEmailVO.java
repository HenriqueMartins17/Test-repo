package com.apitable.enterprise.apitablebilling.appsumo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Appsumo user email VO.
 */
@Data
@Builder(toBuilder = true)
@Schema(description = "Appsumo user email vo")
public class UserEmailVO {

    @Schema(description = "email", type = "java.lang.String", example = "***")
    private String email;
}
