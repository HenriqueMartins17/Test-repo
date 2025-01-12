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

package com.apitable.enterprise.vikabilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.vikabilling.model.ProductPriceVo;
import com.apitable.enterprise.vikabilling.service.IShopService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Store API.
 */
@RestController
@Tag(name = "Store API")
@ApiResource(path = "/shop")
@Slf4j
public class ShopController {

    @Resource
    private IShopService iShopService;

    @GetResource(path = "/prices", requiredPermission = false)
    @Operation(summary = "Get Price List for A Product", description = "Self-operated product "
        + "price list")
    @Parameters({
        @Parameter(name = "product", description = "product name", required = true, schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "SILVER"),
    })
    public ResponseData<List<ProductPriceVo>> getPrices(
        @RequestParam("product") String productName) {
        List<ProductPriceVo> prices = iShopService.getPricesByProduct(productName);
        return ResponseData.success(prices);
    }
}
