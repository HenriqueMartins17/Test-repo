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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p>
 * App
 * </p>
 */
@Data
public class App {

    private String appType;

    @JsonProperty("disable")
    private boolean disable;

    private Image logo;

    private String appInfo;

    private String htmlStr;

    private String note;

    private String appName;

    private String type;

    private String appDescription;

    private String id;

    private Image image;

    private String appId;

    private BtnCard btnCard;

    private List<String> env;

    private Integer displayOrder;
}
