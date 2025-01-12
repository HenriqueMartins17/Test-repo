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

package com.apitable.enterprise.social.infrastructure.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Data;

@Data
public class WoaDepartmentResponse {

    /**
     * Status code, non-zero means failure
     */
    private int result;

    /**
     * Department List
     */
    private List<Dept> depts;

    @Data
    public static class Dept {
        /**
         * Parent department id
         */
        @JsonProperty("dept_pid")
        private String deptPid;
        /**
         * Department id
         */
        @JsonProperty("dept_id")
        private String deptId;

        /**
         * Department name
         */
        private String name;

        /**
         * Department creation time, timestamp in seconds
         */
        private int ctime;

        /**
         * Department sorting field, the larger the value, the higher the sorting priority
         */
        private int order;
    }
}
