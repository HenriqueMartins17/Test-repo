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

package com.apitable.enterprise.elink.autoconfigure;


import java.util.ArrayList;
import java.util.List;

import com.apitable.enterprise.elink.autoconfigure.ElinkProperties.AgentAppProperty;
import com.apitable.enterprise.elink.infrastructure.ElinkConnector;
import com.apitable.enterprise.elink.infrastructure.ElinkTemplate;
import com.apitable.enterprise.elink.infrastructure.ElinkTemplate.AgentApp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "connector.elink.enabled", havingValue = "true")
@EnableConfigurationProperties(ElinkProperties.class)
public class ElinkAutoConfiguration {

    private final ElinkProperties properties;

    public ElinkAutoConfiguration(ElinkProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "elinkConnector")
    @ConditionalOnMissingBean
    public ElinkConnector connector() {
        List<AgentApp> agentAppList = new ArrayList();
        if (!properties.getAgentApp().isEmpty()){
            for (AgentAppProperty appInfo : properties.getAgentApp()){
                AgentApp agentApp = new AgentApp();
                agentApp.setAgentId(appInfo.getAgentId());
                agentApp.setCallbackDomain(appInfo.getCallbackDomain());
                agentApp.setQrDomain(appInfo.getQrDomain());
                agentApp.setAgentSecret(appInfo.getAgentSecret());
                agentAppList.add(agentApp);
            }
        }
        return new ElinkTemplate(properties.getCorpId(), agentAppList, properties.getBaseUrl());
    }
}
