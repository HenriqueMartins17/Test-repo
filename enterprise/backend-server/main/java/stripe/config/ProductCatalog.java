package com.apitable.enterprise.stripe.config;

import com.apitable.enterprise.apitablebilling.enums.ProductEnum;
import java.util.List;
import java.util.Optional;
import lombok.Data;

/**
 * product catalog json object.
 */
@Data
public class ProductCatalog {

    private List<Product> products;

    /**
     * filter hidden product.
     * this method will return a new ProductCatalog object.
     *
     * @return ProductCatalog
     */
    public ProductCatalog filterHidden() {
        ProductCatalog productCatalog = new ProductCatalog();
        productCatalog.setProducts(products.stream()
            .filter(product -> !product.isHidden())
            .toList());
        return productCatalog;
    }

    /**
     * find product object through by product id.
     * return not hidden product
     *
     * @param productId product id
     * @return Product
     */
    public Optional<Product> findByProductId(String productId) {
        return products.stream()
            .filter(product -> product.getId().equals(productId))
            .findFirst();
    }

    /**
     * find product object through by product name.
     *
     * @param productName product name
     * @return Product
     */
    public Optional<Product> findByProductName(String productName) {
        return products.stream()
            .filter(product -> product.getName().equalsIgnoreCase(productName))
            .findFirst();
    }

    public Optional<Product> freeProduct() {
        return findByProductName(ProductEnum.FREE.getName());
    }

    /**
     * find product object through by price id.
     * return not hidden product
     *
     * @param priceId price id
     * @return Product
     */
    public Optional<Product> findByPriceId(String priceId) {
        return products.stream()
            .filter(product -> product.findPrice(priceId).isPresent())
            .findFirst();
    }

}
