package com.apitable.enterprise.apitablebilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.stripe.config.PricingTable;
import com.apitable.enterprise.stripe.config.ProductCatalog;
import com.apitable.enterprise.stripe.config.ProductCatalogFactory;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Price Api")
@ApiResource
@Slf4j
public class PriceController {

    @GetResource(path = "/prices", requiredLogin = false)
    public ResponseData<PricingTable> getPricing() {
        ProductCatalog productCatalog = ProductCatalogFactory.INSTANCE;
        PricingTable pricingTable = PricingTable.build(productCatalog.filterHidden());
        return ResponseData.success(pricingTable);
    }
}
