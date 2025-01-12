/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.idaas.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

/**
 * <p>
 * IDaaS Create tenant
 * </p>
 */
@Schema(description = "IDaaS Create tenant")
@Setter
@Getter
@ToString
@EqualsAndHashCode
@Validated
public class IdaasTenantCreateRo {

    @Schema(description = "Tenant name. Can only be lowercase letters and numbers, and cannot start with a number")
    @NotBlank
    private String tenantName;

    @Schema(description = "Enterprise name. Full Chinese name")
    @NotBlank
    private String corpName;

    @Schema(description = "Default administrator account")
    @NotBlank
    private String adminUsername;

    @Schema(description = "Default Administrator Password")
    @NotBlank
    private String adminPassword;

    @Schema(description = "System level ServiceAccount")
    @NotNull
    @Valid
    private ServiceAccount serviceAccount;

    @Setter
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class ServiceAccount {

        @Schema(description = "Client ID")
        @NotBlank
        private String clientId;

        @Schema(description = "Private Key")
        @NotNull
        @Valid
        private PrivateKey privateKey;

        @Setter
        @Getter
        @ToString
        @EqualsAndHashCode
        public static class PrivateKey {

            @NotBlank
            private String p;

            @NotBlank
            private String kty;

            @NotBlank
            private String q;

            @NotBlank
            private String d;

            @NotBlank
            private String e;

            @NotBlank
            private String use;

            @NotBlank
            private String kid;

            @NotBlank
            private String qi;

            @NotBlank
            private String dp;

            @NotBlank
            private String dq;

            @NotBlank
            private String n;

        }

    }

}
