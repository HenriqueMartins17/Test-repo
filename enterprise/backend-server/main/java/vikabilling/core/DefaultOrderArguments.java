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

import com.apitable.enterprise.vikabilling.model.CreateOrderRo;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.setting.Price;

public class DefaultOrderArguments implements OrderArguments {

    private final String spaceId;

    private final Price price;

    public DefaultOrderArguments(String spaceId, Price price) {
        this.spaceId = spaceId;
        this.price = price;
    }

    public DefaultOrderArguments(final CreateOrderRo input) {
        if (input == null) {
            this.spaceId = null;
            this.price = null;
        }
        else {
            this.spaceId = input.getSpaceId();
            this.price = BillingConfigManager.getPriceBySeatAndMonth(input.getProduct(), input.getSeat(), input.getMonth());
        }
    }

    @Override
    public String getSpaceId() {
        return this.spaceId;
    }

    @Override
    public Price getPrice() {
        return this.price;
    }
}
