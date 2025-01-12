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

package com.apitable.enterprise.vika.core.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class OrderTypeSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        switch (value) {
            case "BUY":
                gen.writeString("新购");
                break;
            case "RENEW":
                gen.writeString("续订");
                break;
            case "UPGRADE":
                gen.writeString("升级");
                break;
            default:
                throw new IllegalArgumentException("无法识别");
        }
    }
}
