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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * Information of WeCom third-party application after login.
 * </p>
 */
@Schema(description = "Information of WeCom third-party application after login")
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class WeComIsvUserLoginVo {

    /**
     * Whether you have logged in. 0: No; 1: Yes
     */
    @Schema(description = "Whether you have logged in. 0: No; 1: Yes")
    private Integer logined;

    /**
     * App Suite ID.
     */
    @Schema(description = "App Suite ID")
    private String suiteId;

    /**
     * Authorized enterprise ID.
     */
    @Schema(description = "Authorized enterprise ID")
    private String authCorpId;

    /**
     * Space ID bound by the application.
     */
    @Schema(description = "Space ID bound by the application")
    private String spaceId;

    /**
     * Whether the address book is being synchronized. 0: No; 1: Yes
     */
    @Schema(description = "Whether the address book is being synchronized. 0: No; 1: Yes")
    private Integer contactSyncing;

    /**
     * User's default member name.
     */
    @Schema(description = "User's default member name")
    private String defaultName;

    /**
     * Whether the default name needs to be changed. 0: No; 1: Yes
     */
    @Schema(description = "Whether the default name needs to be changed. 0: No; 1: Yes")
    private Integer shouldRename;

    /**
     * Whether manual authorization is required again. 0: No; 1: Yes
     */
    @Schema(description = "Whether manual authorization is required again. 0: No; 1: Yes")
    private Integer shouldReAuth;

}
