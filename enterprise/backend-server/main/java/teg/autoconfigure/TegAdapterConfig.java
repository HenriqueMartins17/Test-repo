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

package com.apitable.enterprise.teg.autoconfigure;

import static com.apitable.auth.enums.AuthException.UNAUTHORIZED;

import com.apitable.core.support.ResponseData;
import com.apitable.shared.component.scanner.ApiResourceFactory;
import com.apitable.shared.constants.FilterConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Teg Adapter config.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "teg.enabled", havingValue = "true")
public class TegAdapterConfig {

    /**
     * Return custom authorized response.
     *
     * @param objectMapper object mapper
     * @return UnauthorizedResponseCustomizer
     */
    @Primary
    @Bean
    public UnauthorizedResponseCustomizer noAuthResponseCustomizer(
        final ObjectMapper objectMapper) {
        return response -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            try (Writer writer = response.getWriter()) {
                objectMapper.writeValue(writer, ResponseData.error(UNAUTHORIZED.getCode(),
                    UNAUTHORIZED.getMessage()));
                writer.flush();
            } catch (IOException e) {
                throw new ServletException(e);
            }
        };
    }

    /**
     * RegistrationBean for JwtProxyUserDetailFilter.
     *
     * @param beanFactory     bean factory
     * @param resourceFactory resource factory
     * @return JwtProxyUserDetailFilter
     */
    @Bean
    FilterRegistrationBean<JwtProxyUserDetailFilter> jwtProxyUserDetailFilter(
        final BeanFactory beanFactory, final ApiResourceFactory resourceFactory) {
        FilterRegistrationBean<JwtProxyUserDetailFilter> filterRegistrationBean =
            new FilterRegistrationBean<>(
                new JwtProxyUserDetailFilter(beanFactory, resourceFactory));
        filterRegistrationBean.setOrder(FilterConstants.TRACE_REQUEST_FILTER);
        filterRegistrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}