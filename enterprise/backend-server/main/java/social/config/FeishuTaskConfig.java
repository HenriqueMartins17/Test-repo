package com.apitable.enterprise.social.config;

import com.apitable.shared.config.configure.TaskDecoratorWrapper;
import com.vikadata.social.feishu.FeishuConfigStorageHolder;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class FeishuTaskConfig {

    @Bean
    TaskDecoratorWrapper taskDecoratorWrapper() {
        return FeishuConfigStorageHolder::remove;
    }
}
