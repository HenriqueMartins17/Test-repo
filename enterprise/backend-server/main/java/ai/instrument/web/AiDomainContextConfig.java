package com.apitable.enterprise.ai.instrument.web;

import com.apitable.enterprise.ai.interfaces.facade.EnterpriseAiServiceFacadeImpl;
import com.apitable.enterprise.ai.service.IAiCreditTransactionOverallService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.interfaces.ai.facade.AiServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI Domain business context config.
 *
 * @author Shawn Deng
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "ai.enabled", havingValue = "true")
public class AiDomainContextConfig {

    @Bean
    @Primary
    public AiServiceFacade enterpriseAiServiceFacade(
        IAiService aiService,
        IAiCreditTransactionService aiCreditTransactionService,
        IAiCreditTransactionOverallService aiCreditTransactionOverallService) {
        return new EnterpriseAiServiceFacadeImpl(aiService, aiCreditTransactionService,
            aiCreditTransactionOverallService);
    }
}
