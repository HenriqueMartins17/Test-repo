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

package com.apitable.enterprise.appstore.setting;

import lombok.Data;

/**
 * <p>
 * App Store
 * </p>
 */
@Data
public class AppStore {

    private String id;

    private String appName;

    private Appendix logo;

    private String type;

    private String appType;

    private String intro;

    private String description;

    private String status;

    private Integer displayOrder;

    private Appendix inlineImage;

    private String notice;

    private boolean needConfigured;

    private String configureUrl;

    private boolean needAuthorize;

    private String helpUrl;

    private String stopActionUrl;

}
