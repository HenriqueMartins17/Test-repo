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

package com.apitable.enterprise.track.autoconfigure;

import com.apitable.enterprise.track.core.DataTracker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * <p>
 * Automatic configuration of data analysis.
 * </p>
 *
 * @author Chambers
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DataAnalyzeProperties.class)
@ConditionalOnClass(DataTracker.class)
@ConditionalOnProperty(value = "data.analyze.enabled", havingValue = "true")
@Import({ SensorsAutoConfiguration.class, PostHogAutoConfiguration.class })
public class DataAnalyzeAutoConfiguration {

}
