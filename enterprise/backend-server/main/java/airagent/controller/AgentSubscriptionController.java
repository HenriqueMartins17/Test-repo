package com.apitable.enterprise.airagent.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.airagent.model.billing.Subscription;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * agent subscription controller.
 */
@RestController
@Tag(name = "AirAgent - Subscription")
@ApiResource(path = "/airagent")
@Slf4j
public class AgentSubscriptionController {

    @GetResource(path = "/subscriptions", requiredPermission = false)
    public ResponseData<Subscription> getSubscriptions() {
        Long userId = SessionContext.getUserId();
        return ResponseData.success(null);
    }
}
