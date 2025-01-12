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

package com.apitable.enterprise.vikabilling.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * Pay Order Request Object.
 * </p>
 */
@Data
@Schema(description = "Pay Order Request Object")
public class PayOrderRo {

    @Schema(description = "order no", example = "SILVER")
    @Deprecated
    private String orderNo;

    @Schema(description = "payment channel type (wx_pub_qr: WeChat Native payment, "
        + "alipay_pc_direct: Alipay computer website payment)", example = "wx_pub_qr")
    private String payChannel;
}
