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
 * Order Payment View.
 * </p>
 */
@Data
@Schema(description = "Order Payment View")
public class OrderPaymentVo {

    @Schema(description = "order no", example = "20220215185035483353")
    private String orderNo;

    @Schema(description = "pay transaction no", example = "20220215185035483353")
    private String payTransactionNo;

    @Schema(description = "payment channel=wx_pub_qr, QR code of WeChat payment", example =
        "weixin://wxpay/bizpayurl?pr=qnZDTZm")
    private String wxQrCodeLink;

    @Schema(description = "payment channel=alipay_pc_direct, the charge object paid by Alipay "
        + "computer website", example = "weixin://wxpay/bizpayurl?pr=qnZDTZm")
    private String alipayPcDirectCharge;
}
