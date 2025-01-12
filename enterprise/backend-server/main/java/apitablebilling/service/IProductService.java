package com.apitable.enterprise.apitablebilling.service;

import com.apitable.enterprise.apitablebilling.model.vo.ProductVO;
import java.util.List;

@Deprecated(since = "1.7.0", forRemoval = true)
public interface IProductService {

    /**
     * product view.
     *
     * @return product view
     */
    List<ProductVO> getProductList();
}
