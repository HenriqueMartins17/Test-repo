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

package com.apitable.enterprise.appstore.model;

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.core.io.IoUtil;
import com.apitable.FileHelper;
import com.apitable.enterprise.appstore.enums.AppType;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * Instance configuration
 */
public class AppInstanceConfigTest {

    @Test
    public void testLarkConfigFromString() {
        InputStream inputStream =
            FileHelper.getInputStreamFromResource("enterprise/lark_config.json");
        String jsonString = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        InstanceConfig config = LarkInstanceConfig.fromJsonString(jsonString);
        assertThat(config).isNotNull();
        assertThat(config.getType()).isEqualTo(AppType.LARK);
        assertThat(config.getProfile().getAppKey()).isNotBlank().isEqualTo("c123123");
    }

    @Test
    public void testLarkConfigToString() throws JSONException {
        LarkInstanceConfigProfile profile = new LarkInstanceConfigProfile("123456", "shag213123");
        LarkInstanceConfig config = new LarkInstanceConfig(profile);
        String data = config.toJsonString();
        String expected =
            "{\"profile\":{\"contactSyncDone\":false,\"eventCheck\":false,\"isConfigComplete\":false,\"appKey\":\"123456\",\"appSecret\":\"shag213123\"},\"type\":\"LARK\"}";
        JSONAssert.assertEquals(expected, data, false);
    }
}
