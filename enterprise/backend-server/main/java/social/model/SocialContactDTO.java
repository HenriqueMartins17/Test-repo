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

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Social Contact User Information.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialContactDTO {

    private SocialDepartment department;

    private Map<String, SocialUser> userMap = new HashMap<>();

    /**
     * DingTalk User DTO.
     */
    @Data
    @Builder(toBuilder = true)
    public static class SocialUser {

        private String openId;

        private String unionId;

        private String name;

        private String avatar;

        private String position;

        private Boolean active;

        private String mobile;

        private String email;

    }

    @Data
    @Builder(toBuilder = true)
    public static class SocialDepartment {

        private String deptName;

        private String deptId;

        private String parentDeptId;
    }
}
