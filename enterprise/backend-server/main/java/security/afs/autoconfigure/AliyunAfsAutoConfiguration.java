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

package com.apitable.enterprise.security.afs.autoconfigure;

import com.aliyuncs.afs.model.v20180112.AnalyzeNvcRequest;
import com.aliyuncs.afs.model.v20180112.AnalyzeNvcResponse;
import com.apitable.enterprise.security.afs.core.AfsCheckerFactory;
import com.apitable.enterprise.security.afs.core.AliyunAfsCheckerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;


/**
 * <p>
 * Alibaba Cloud Human-Machine Authentication Service Configuration.
 * </p>
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ AnalyzeNvcRequest.class, AnalyzeNvcResponse.class })
@ConditionalOnProperty(value = "afs.type", havingValue = "aliyun")
public class AliyunAfsAutoConfiguration {

    private final AfsProperties properties;

    public AliyunAfsAutoConfiguration(AfsProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AfsCheckerFactory afsCheckerFactory() {
        AfsProperties.Aliyun aliyun = properties.getAliyun();
        Assert.state(aliyun != null, "human-machine verification has not been configured");
        return new AliyunAfsCheckerFactory(aliyun.getRegionId(), aliyun.getAccessKeyId(),
            aliyun.getSecret());
    }
}
