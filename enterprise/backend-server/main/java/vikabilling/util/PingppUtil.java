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

package com.apitable.enterprise.vikabilling.util;

import static com.apitable.enterprise.vikabilling.enums.OrderException.PAY_ORDER_FAIL;
import static com.apitable.enterprise.vikabilling.enums.PayChannel.ALIPAY_PC;
import static com.apitable.enterprise.vikabilling.enums.PayChannel.WX_PUB_QR;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.HttpContextUtil;
import com.apitable.enterprise.vikabilling.enums.PayChannel;
import com.apitable.enterprise.vikabilling.model.ChargeSuccess;
import com.apitable.enterprise.vikabilling.setting.Price;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.pingplusplus.exception.PingppException;
import com.pingplusplus.model.Charge;
import com.pingplusplus.model.Event;
import com.pingplusplus.model.Webhooks;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping++ Util
 *
 * @author Shawn Deng
 */
public class PingppUtil {

    private static final Logger log = LoggerFactory.getLogger(PingppUtil.class);

    public static final String PINGPP_SIGNATURE = "x-pingplusplus-signature";

    public static final String CHARGE_SUCCESS = "charge.succeeded";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * get event data from request
     *
     * @param requestBody request body
     * @param signature   signature
     * @return Event Object
     * @throws Exception exception
     */
    public static Event getEventFromRequest(String requestBody, String signature) throws Exception {

        boolean verify = verifyData(requestBody, signature, PingPublicKeyLoader.getPublicKey());
        if (!verify) {
            throw new RuntimeException("illegal pay callback request");
        }
        return Webhooks.eventParse(requestBody);
    }

    /**
     * verify signature
     *
     * @param dataString      request data
     * @param signatureString signature
     * @param publicKey       Ping++ public key
     * @return true | false
     * @throws NoSuchAlgorithmException algorithm exception
     * @throws InvalidKeyException      unable to check exception
     * @throws SignatureException       signature exception
     */
    public static boolean verifyData(String dataString, String signatureString, PublicKey publicKey)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signatureBytes = Base64.decodeBase64(signatureString);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(dataString.getBytes(StandardCharsets.UTF_8));
        return signature.verify(signatureBytes);
    }

    public static ChargeSuccess parsePingChargeSuccessData(String eventData) {
        try {
            TypeReference<ChargeSuccess> typeReference =
                new TypeReference<>() {
                };
            return MAPPER.readValue(eventData, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Charge createCharge(String appId, Price price, PayChannel channel,
                                      String outOrderNo, int amount) {
        // create payment serial number
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("order_no", outOrderNo);
        chargeParams.put("amount", amount);
        chargeParams.put("app", Collections.singletonMap("id", appId));
        chargeParams.put("channel", channel.getName());
        chargeParams.put("currency", "cny");
        chargeParams.put("client_ip", HttpContextUtil.getRemoteAddr(HttpContextUtil.getRequest()));
        chargeParams.put("subject", price.getGoodChTitle());
        chargeParams.put("body", price.getGoodChTitle());
        Map<String, Object> extra = new HashMap<>();
        if (channel == WX_PUB_QR) {
            extra.put("product_id", price.getProduct());
        } else if (channel == ALIPAY_PC) {
            extra.put("qr_pay_mode", 4);
            extra.put("qrcode_width", 208);
        }
        chargeParams.put("extra", extra);
        try {
            // initiate a transaction request
            return Charge.create(chargeParams);
        } catch (PingppException e) {
            log.error("failed to initiate order payment", e);
            throw new BusinessException(PAY_ORDER_FAIL);
        }
    }
}
