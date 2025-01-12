package com.apitable.enterprise.apitablebilling.model.ro;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * checkout create request parameter.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutCreation {

    @NotBlank(message = "spaceId is required")
    private String spaceId;

    @NotBlank(message = "priceId is required")
    private String priceId;

    private Boolean trial = false;

    private String clientReferenceId;

    private String couponId;

    private String mode;
}
