package com.apitable.enterprise.apitablebilling.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.apitablebilling.model.vo.ProductVO;
import com.apitable.enterprise.apitablebilling.service.IProductService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * product resource.
 */
@RestController
@Tag(name = "Product Api")
@ApiResource
@Slf4j
@Deprecated(since = "1.7.0", forRemoval = true)
public class ProductController {

    @Resource
    private IProductService iProductService;

    @GetResource(path = "/products", requiredLogin = false)
    public ResponseData<List<ProductVO>> getList() {
        return ResponseData.success(iProductService.getProductList());
    }
}
