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

import static com.vikadata.social.feishu.constants.FeishuConstants.FEISHU_ROOT_DEPT_ID;

import cn.hutool.json.JSONObject;
import com.vikadata.social.feishu.model.v3.FeishuDeptObject;
import com.vikadata.social.feishu.model.v3.FeishuUserObject;
import java.util.List;
import lombok.Data;

/**
 * Lark Tenant Address Book Authorization Scope Processing Class.
 */
@Data
public class FeishuContactAuthScope {

    private List<FeishuDeptObject> departments;

    private List<FeishuUserObject> users;

    public FeishuContactAuthScope() {
    }

    /**
     * createRootDeptObject.
     */
    public static FeishuDeptObject createRootDeptObject() {
        FeishuDeptObject deptObject = new FeishuDeptObject();
        deptObject.setDepartmentId(FEISHU_ROOT_DEPT_ID);
        deptObject.setOpenDepartmentId(FEISHU_ROOT_DEPT_ID);
        deptObject.setOrder("0");
        return deptObject;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.putOnce("departments", this.departments);
        object.putOnce("users", this.users);
        return object.toString();
    }
}
