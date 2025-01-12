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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Get the list of user groups
 * </p>
 *
 */
@Setter
@Getter
public class GroupsRequest {

    /**
     * start page number. start from 0
     */
    @JsonProperty("page_index")
    private Integer pageIndex;

    /**
     * page size
     */
    @JsonProperty("page_size")
    private Integer pageSize;

    /**
     * sort field, preceded by {@code _} indicates ascending order, otherwise descending order
     */
    @JsonProperty("order_by")
    private List<String> orderBy;

}
