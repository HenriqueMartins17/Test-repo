package com.apitable.enterprise;

import com.apitable.AbstractIntegrationTest;
import com.apitable.enterprise.airagent.service.IAgentAuthService;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.shared.config.initializers.EnterpriseEnvironmentInitializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = {
    EnterpriseEnvironmentInitializers.class,
    TestAirAgentContextInitializer.class
})
public class AbstractAirAgentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    protected IAgentAuthService iAgentAuthService;

    @Autowired
    protected IAgentService iAgentService;
}
