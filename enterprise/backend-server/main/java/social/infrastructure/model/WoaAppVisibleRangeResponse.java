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
import lombok.Getter;
import lombok.Setter;

@Data
public class WoaAppVisibleRangeResponse {

    /**
     * Status code, non-zero means failure
     */
    private int result;

    public List<User> users;

    public List<Department> departments;

    public Company company;

    @Setter
    @Getter
    public static class User {

        @JsonProperty("company_uid")
        private String companyUid;
    }

    @Setter
    @Getter
    public static class Department {

        @JsonProperty("dept_id")
        private String deptId;
    }

    @Setter
    @Getter
    public static class Company {

        @JsonProperty("company_id")
        private String companyId;

        private String name;
    }
}
