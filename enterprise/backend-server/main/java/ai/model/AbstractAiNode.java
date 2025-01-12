package com.apitable.enterprise.ai.model;

import cn.hutool.json.JSONUtil;
import com.apitable.workspace.model.NodeObject;

/**
 * AI datasheet setting.
 *
 * @author Shawn Deng
 */
public abstract class AbstractAiNode extends NodeObject implements AiNodeInterface {

    @Override
    public String toJson() {
        return JSONUtil.toJsonStr(this);
    }
}
