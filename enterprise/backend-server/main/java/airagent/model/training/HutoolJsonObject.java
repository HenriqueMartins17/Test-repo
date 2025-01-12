package com.apitable.enterprise.airagent.model.training;

import cn.hutool.json.JSONUtil;

/**
 * hutool json object.
 */
public abstract class HutoolJsonObject implements JsonObject {

    @Override
    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
