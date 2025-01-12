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

import static com.apitable.enterprise.vikabilling.util.OrderUtil.toCurrencyUnit;
import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.enterprise.vikabilling.enums.ProductEnum;
import com.apitable.enterprise.vikabilling.model.ProductPriceVo;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Shop Service Implement Test
 */
public class ShopServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Test
    public void testGetPricesByProductOnSilver() {
        List<ProductPriceVo> productPriceVos = isShopService.getPricesByProduct(ProductEnum.SILVER.name());
        // assert plan in silver product
        assertThat(productPriceVos).isNotEmpty();
    }

    @Test
    public void testGetPricesByProductOnGold() {
        List<ProductPriceVo> productPriceVos = isShopService.getPricesByProduct(ProductEnum.GOLD.name());
        // assert plan in gold product
        assertThat(productPriceVos).isNotEmpty();
    }

    @Test
    public void testGetBeforeEventDiscountPriceOnSilver() {
        final OffsetDateTime nowTime = OffsetDateTime.of(2022, 10, 23, 17, 0, 0, 0, getTestTimeZone());
        getClock().setTime(nowTime);
        // The discount price does not exist until the event starts
        List<ProductPriceVo> productPriceVos = isShopService.getPricesByProduct(ProductEnum.SILVER.name());
        productPriceVos.forEach(priceVo -> assertThat(priceVo.getPriceDiscount()).isEqualTo(toCurrencyUnit(BigDecimal.ZERO)));
    }

    @Test
    public void testGetExpireEventDiscountPriceOnSilver() {
        final OffsetDateTime nowTime = OffsetDateTime.of(2022, 11, 12, 0, 0, 0, 0, getTestTimeZone());
        getClock().setTime(nowTime);
        // The event has expired, and the discount price does not exist
        List<ProductPriceVo> productPriceVos = isShopService.getPricesByProduct(ProductEnum.SILVER.name());
        productPriceVos.forEach(priceVo -> assertThat(priceVo.getPriceDiscount()).isEqualTo(toCurrencyUnit(BigDecimal.ZERO)));
    }

    @Test
    public void testGetLastEventDateDiscountPriceOnSilver() {
        final OffsetDateTime nowTime = OffsetDateTime.of(2022, 11, 11, 23, 23, 30, 0, getTestTimeZone());
        getClock().setTime(nowTime);
        // The event has expired, and the discount price does not exist
        List<ProductPriceVo> productPriceVos = isShopService.getPricesByProduct(ProductEnum.SILVER.name());
        productPriceVos
                .stream().filter(p -> p.getSeat() != 2 && p.getMonth() != 1)
                .forEach(priceVo -> assertThat(priceVo.getPriceDiscount()).isNotEqualTo(BigDecimal.ZERO));
    }
}
