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

package com.apitable.enterprise.vikabilling.core;

import com.apitable.enterprise.vikabilling.enums.DryRunAction;
import com.apitable.enterprise.vikabilling.enums.DryRunType;
import com.apitable.enterprise.vikabilling.model.DryRunOrderArgs;


/**
 * Dry Run Argument
 */
public class DryRunArguments {

    private final DryRunType dryRunType;

    private final DryRunAction action;

    private final String spaceId;

    private final String product;

    private final Integer seat;

    private final Integer month;

    public DryRunArguments(DryRunType dryRunType, DryRunAction action, String spaceId, String product, Integer seat, Integer month) {
        this.dryRunType = dryRunType;
        this.action = action;
        this.spaceId = spaceId;
        this.product = product;
        this.seat = seat;
        this.month = month;
    }

    public DryRunArguments(DryRunOrderArgs input) {
        this.dryRunType = input.getDryRunType() == null ? DryRunType.SUBSCRIPTION_ACTION : DryRunType.valueOf(input.getDryRunType());
        this.action = DryRunAction.valueOf(input.getAction());
        this.spaceId = input.getSpaceId();
        this.product = input.getProduct();
        this.seat = input.getSeat();
        this.month = input.getMonth();
    }

    public DryRunType getDryRunType() {
        return dryRunType;
    }

    public DryRunAction getAction() {
        return action;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public String getProduct() {
        return product;
    }

    public Integer getSeat() {
        return seat;
    }

    public Integer getMonth() {
        return month;
    }
}
