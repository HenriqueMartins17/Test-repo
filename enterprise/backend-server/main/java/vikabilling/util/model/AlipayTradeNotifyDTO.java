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

package com.apitable.enterprise.vikabilling.util.model;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * charge dto.
 */
@Data
public class AlipayTradeNotifyDTO {

    private LocalDateTime notifyTime;

    private String notifyType;

    private String notifyId;

    private String charset;

    private String version;

    private String signType;

    private String sign;

    private String authAppId;

    private String tradeNo;

    private String appId;

    private String outTradeNo;

    private String outBizNo;

    private String buyerId;

    private String sellerId;

    private String tradeStatus;

    private Double totalAmount;

    private Double receiptAmount;

    private Double invoiceAmount;

    private Double buyerPayAmount;

    private Double pointAmount;

    private Double refundFee;

    private String subject;

    private String body;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtPayment;

    private LocalDateTime gmtRefund;

    private LocalDateTime gmtClose;

    private String fundBillList;

    private String vocherDetailList;

    private String passbackParams;
}
