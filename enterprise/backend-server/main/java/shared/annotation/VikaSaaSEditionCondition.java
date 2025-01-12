package com.apitable.enterprise.shared.annotation;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * edition is vika-saas.
 *
 * @author Shawn Deng
 */
public class VikaSaaSEditionCondition extends AnyNestedCondition {

    public VikaSaaSEditionCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(value = "edition", havingValue = "vika-saas")
    static class OnVikaSaaS {
    }

}
