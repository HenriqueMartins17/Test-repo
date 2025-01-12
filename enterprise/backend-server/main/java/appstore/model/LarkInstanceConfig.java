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

package com.apitable.enterprise.appstore.model;

import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.appstore.enums.AppType;

/**
 * Lark instance interface.
 */
public class LarkInstanceConfig implements InstanceConfig {

    private AppType type = AppType.LARK;

    private LarkInstanceConfigProfile profile;

    public LarkInstanceConfig(LarkInstanceConfigProfile profile) {
        this.profile = profile;
    }

    public static LarkInstanceConfig fromJsonString(String json) {
        return JSONUtil.toBean(json, LarkInstanceConfig.class);
    }

    @Override
    public String toJsonString() {
        return JSONUtil.toJsonStr(this);
    }

    public void setProfile(LarkInstanceConfigProfile profile) {
        this.profile = profile;
    }

    public void setType(AppType type) {
        this.type = type;
    }

    @Override
    public InstanceConfigProfile getProfile() {
        return profile;
    }

    @Override
    public AppType getType() {
        return type;
    }

}
