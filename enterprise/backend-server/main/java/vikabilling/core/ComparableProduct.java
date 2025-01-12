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

import com.apitable.enterprise.vikabilling.enums.ProductEnum;

/**
 * Subscription Product
 */
public class ComparableProduct implements Comparable<ComparableProduct> {

    private ProductEnum product;

    public ComparableProduct(ProductEnum product) {
        this.product = product;
    }

    @Override
    public int compareTo(ComparableProduct other) {
        return product.getRank() - other.getProduct().getRank();
    }

    public boolean isEqual(ComparableProduct other) {
        return this.compareTo(other) == 0;
    }

    public boolean isGreaterThan(ComparableProduct other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessThan(ComparableProduct other) {
        return this.compareTo(other) < 0;
    }

    public ProductEnum getProduct() {
        return product;
    }

    public void setProduct(ProductEnum product) {
        this.product = product;
    }
}
