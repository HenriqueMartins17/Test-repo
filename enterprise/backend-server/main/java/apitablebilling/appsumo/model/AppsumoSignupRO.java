package com.apitable.enterprise.apitablebilling.appsumo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Appsumo signup RO.
 */
@Data
@Schema(description = "Appsumo signup ro")
public class AppsumoSignupRO {

    @Schema(description = "Email key", example = "*****", requiredMode = Schema.RequiredMode.REQUIRED)
    private String state;

    @Schema(description = "Credential(password/verify code...)",  example = "qwer1234 || 261527", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
