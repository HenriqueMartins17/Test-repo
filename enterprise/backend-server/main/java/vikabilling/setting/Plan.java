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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Data;

/**
 * <p>
 * Billing Plan
 * </p>
 */
@Data
public class Plan {

    private String id;

    private String description;

    private String product;

    private String productCategory;

    private boolean canTrial;

    private String channel;

    private int seats;

    private List<String> features;

    @JsonProperty("gift")
    private boolean isGift;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Plan plan = (Plan) o;

        return getId().equals(plan.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
