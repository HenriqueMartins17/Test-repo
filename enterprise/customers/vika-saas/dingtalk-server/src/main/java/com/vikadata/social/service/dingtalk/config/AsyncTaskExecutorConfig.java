/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.social.service.dingtalk.config;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * asynchronous task configuration,
 * thread pool encapsulated by spring framework
 * ROLE_INFRASTRUCTURE: Spring framework's own BEAN, which has nothing to do with the user
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class AsyncTaskExecutorConfig {

    @Bean
    TaskExecutorCustomizer taskExecutorCustomizer() {
        return executor -> {
            executor.setCorePoolSize(4);
            executor.setMaxPoolSize(50);
            executor.setQueueCapacity(500);
            executor.setKeepAliveSeconds(3000);
            executor.setThreadNamePrefix("thread-task-");
            executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        };
    }

    @Bean
    TaskDecorator taskDecorator() {
        return runnable -> {
            RequestAttributes context = RequestContextHolder.getRequestAttributes();
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    RequestContextHolder.setRequestAttributes(context);
                    if (mdcContext != null) {
                        MDC.setContextMap(mdcContext);
                    }
                    // execute asynchronous tasks
                    runnable.run();
                }
                finally {
                    log.info("Reset asynchronous thread variables");
                    MDC.clear();
                    RequestContextHolder.resetRequestAttributes();
                }
            };
        };
    }
}
