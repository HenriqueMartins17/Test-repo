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
import lombok.Data;

/**
 * <p>
 * Ding Talk User Information.
 * </p>
 */
@Data
public class DingTalkContactDTO {

    private DingTalkDepartmentDTO department;

    private Map<String, DingTalkUserDTO> userMap = new HashMap<>();

    /**
     * DingTalk User DTO.
     */
    @Data
    public static class DingTalkUserDTO {

        private String openId;

        private String unionId;

        private String name;

        private String avatar;

        private String position;

        private Boolean active;

        private String mobile;

        private String email;

    }

    /**
     * DingTalk Department DTO.
     */
    @Data
    public static class DingTalkDepartmentDTO {

        private String deptName;

        private Long deptId;

        private Long parentDeptId;
    }
}
