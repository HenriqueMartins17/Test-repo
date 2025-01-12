package com.apitable.enterprise.apitablebilling.service.impl;

import static com.apitable.enterprise.apitablebilling.model.vo.PriceVO.buildPriceVO;

import com.apitable.enterprise.apitablebilling.model.vo.PriceVO;
import com.apitable.enterprise.apitablebilling.model.vo.ProductVO;
import com.apitable.enterprise.apitablebilling.service.IProductService;
import com.apitable.enterprise.stripe.config.PriceConfigUtil;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Product service implements.
 */
@Service
public class ProductServiceImpl implements IProductService {

    @Override
    public List<ProductVO> getProductList() {
        List<ProductVO> productVOS = new ArrayList<>();
        PriceConfigUtil.priceConfig.forEach((name, product) -> {
            ProductVO productVO = new ProductVO();
            productVO.setId(product.getId());
            productVO.setName(product.getName());
            productVO.setMostPopular(product.isMostPopular());
            List<PriceVO> priceVOS = new ArrayList<>();
            product.getPrices().forEach(price -> priceVOS.add(buildPriceVO(price)));
            productVO.setPrices(priceVOS);
            productVOS.add(productVO);
        });
        return productVOS;
    }
}
