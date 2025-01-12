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

package com.apitable.enterprise.vikabilling.setting;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * <p>
 * Billing Feature
 * </p>
 */
@Data
public class Feature {

    private String id;

    private List<String> plans;

    private String function;

    @JsonProperty("functionType")
    private String functionType;

    private Long specification;

    private String unit;
}
