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

package com.apitable.enterprise.vikabilling.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.model.ProductPriceVo;
import com.apitable.enterprise.vikabilling.service.IShopService;
import com.apitable.enterprise.vikabilling.util.BillingConfigManager;
import com.apitable.enterprise.vikabilling.util.model.BillingPlanPrice;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.enterprise.vikabilling.setting.Price;

import org.springframework.stereotype.Service;

/**
 * <p>
 * Shop Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class ShopServiceImpl implements IShopService {

    @Override
    public List<ProductPriceVo> getPricesByProduct(String productName) {
        ProductEnum product = ProductEnum.valueOf(productName);
        List<Price> prices = BillingConfigManager.getPriceList(product);
        List<ProductPriceVo> planPriceVos = new ArrayList<>(prices.size());
        LocalDate nowDate = ClockManager.me().getLocalDateNow();
        prices.forEach(price -> planPriceVos.add(ProductPriceVo.fromPrice(BillingPlanPrice.of(price, nowDate))));
        return planPriceVos;
    }
}
