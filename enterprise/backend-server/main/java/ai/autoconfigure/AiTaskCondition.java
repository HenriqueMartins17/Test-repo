package com.apitable.enterprise.ai.autoconfigure;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Ai Task Condition.
 *
 * @author Shawn Deng
 */
public class AiTaskCondition extends AnyNestedCondition {

    public AiTaskCondition() {
        super(ConfigurationPhase.PARSE_CONFIGURATION);
    }

    @ConditionalOnProperty(value = "ai.enabled", havingValue = "true")
    static class EnableAiCondition {

    }

    @ConditionalOnProperty(value = "system.test-enabled", havingValue = "false", matchIfMissing = true)
    static class DisableTestCondition {

    }
}
