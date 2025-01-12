package com.apitable.enterprise.stripe.config;

import com.apitable.shared.support.serializer.NullCollectionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import lombok.Data;

/**
 * price pros.
 */
@Data
public class PricingTable {

    @JsonSerialize(nullsUsing = NullCollectionSerializer.class)
    private List<Product> baseProducts;

    @JsonSerialize(nullsUsing = NullCollectionSerializer.class)
    private List<Product> addOnProducts;

    public static PricingTable build(ProductCatalog productCatalog) {
        var pricingTable = new PricingTable();
        pricingTable.setBaseProducts(productCatalog.getProducts());
        return pricingTable;
    }
}
