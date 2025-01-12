/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Third party integrated tenant environment configuration view.
 * </p>
 */
@Data
@Schema(description = "Third party integrated tenant environment configuration view")
public class SocialTenantEnvVo {

    @Schema(description = "Domain name")
    private String domainName;

    @Schema(description = "Tenant Integration Environment Collection")
    private Map<String, Object> envs;

    /**
     * WeComEnv.
     */
    @Data
    @Builder(toBuilder = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Integrated WeCom environment view")
    public static class WeComEnv {

        @Schema(description = "WeCom-Enterprise Id")
        private String corpId;

        @Schema(description = "WeCom-Self built application ID")
        private String agentId;

        @Schema(description = "WeCom-Enable")
        private Boolean enabled;

    }

}
