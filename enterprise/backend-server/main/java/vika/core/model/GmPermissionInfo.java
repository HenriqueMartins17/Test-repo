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

package com.apitable.enterprise.vika.core.model;

import java.util.List;

/**
 * <p>
 * GM Permission configuration information
 * </p>
 *
 */
public class GmPermissionInfo {

    private String action;

    private List<Long> unitIds;

    public GmPermissionInfo() {
    }

    public GmPermissionInfo(String resourceCode, List<Long> unitIds) {
        this.action = resourceCode;
        this.unitIds = unitIds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Long> getUnitIds() {
        return unitIds;
    }

    public void setUnitIds(List<Long> unitIds) {
        this.unitIds = unitIds;
    }
}
