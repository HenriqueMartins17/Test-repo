package com.apitable.enterprise.apitablebilling.model.vo;

import java.util.List;
import lombok.Data;

@Data
@Deprecated(since = "1.7.0", forRemoval = true)
public class ProductVO {

    private String id;

    private String name;

    private boolean mostPopular;

    private List<PriceVO> prices;
}
