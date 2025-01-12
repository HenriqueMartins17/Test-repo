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

package com.apitable.enterprise.idaas.infrastructure.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Get Personnel List
 * </p>
 *
 */
@Setter
@Getter
public class UsersResponse {

    /**
     * total number of filtered results
     */
    private Integer total;

    /**
     * filtered page data
     */
    private List<UserResponse> data;

    @Setter
    @Getter
    public static class UserResponse {

        private String id;

        private String tenantId;

        private Long modifiedOn;

        private Long createdOn;

        private String objectType;

        private Values values;

        @Setter
        @Getter
        public static class Values {

            private String username;

            private String displayName;

            private String primaryMail;

            private String deptId;

            private List<String> groups;

            private String phoneNum;

            private String status;

        }

    }

}
