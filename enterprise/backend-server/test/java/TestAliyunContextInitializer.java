package com.apitable.enterprise;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

public class TestAliyunContextInitializer implements
    ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(TestAliyunContextInitializer.class);

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        log.info("initializing aliyun environment variables");
        TestPropertySourceUtils.addPropertiesFilesToEnvironment(applicationContext,
            "classpath:enterprise/aliyun.properties");
    }
}
