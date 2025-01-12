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

package com.apitable.enterprise.social.model;

import lombok.Data;

/**
 * <p>
 * Ding Talk Creation Template.
 * </p>
 */
@Data
public class DingTalkDaCreateTemplateDTO {

    /**
     * Application instance ID.
     */
    private String bizAppId;

    /**
     * Mobile terminal opening address.
     */
    private String homepageLink;

    /**
     * Opening address of PC terminal.
     */
    private String pcHomepageLink;

    /**
     * Mobile terminal editing page opening address.
     */
    private String homepageEditLink;

    /**
     * Opening address of edit page on PC side.
     */
    private String pcHomepageEditLink;
}
