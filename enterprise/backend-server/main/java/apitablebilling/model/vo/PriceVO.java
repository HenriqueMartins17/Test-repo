package com.apitable.enterprise.apitablebilling.model.vo;

import com.apitable.enterprise.stripe.config.PriceObject;
import lombok.Data;

/**
 * price view.
 */
@Data
@Deprecated(since = "1.7.0", forRemoval = true)
public class PriceVO {

    private String id;

    private String type;

    private String productId;

    private String interval;

    private long amount;

    private long includeUnit;

    private long increaseUnitAmount;

    /**
     * builder.
     *
     * @param priceObject price object
     * @return PriceVO
     */
    public static PriceVO buildPriceVO(PriceObject priceObject) {
        PriceVO priceVO = new PriceVO();
        priceVO.setId(priceObject.getId());
        priceVO.setType(priceObject.getType());
        priceVO.setProductId(priceObject.getProductId());
        priceVO.setInterval(priceObject.getRecurringInterval());
        priceVO.setAmount(priceObject.getAmount());
        priceVO.setIncludeUnit(priceObject.getIncludeUnit());
        priceVO.setIncreaseUnitAmount(priceObject.getPerUnitAmount());
        return priceVO;
    }
}
