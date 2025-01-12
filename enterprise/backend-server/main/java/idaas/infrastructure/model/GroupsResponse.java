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
 * Get the list of user group
 * </p>
 *
 */
@Setter
@Getter
public class GroupsResponse {

    private Integer total;

    private List<GroupResponse> data;

    @Setter
    @Getter
    public static class GroupResponse {

        private String id;

        private String name;

        private String type;

        /**
         * User Group Sort
         *
         * <p>
         * Note: The interface itself does not return the sort value, here is convenient to synchronize to the VIKA for sorting use
         * </p>
         */
        private Integer order;

    }

}
