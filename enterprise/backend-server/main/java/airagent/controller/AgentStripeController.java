package com.apitable.enterprise.airagent.controller;

import cn.hutool.core.util.StrUtil;
import com.apitable.enterprise.apitablebilling.service.IStripeEventHandler;
import com.apitable.enterprise.stripe.core.StripeTemplate;
import com.apitable.enterprise.stripe.service.IStripeEventService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * stripe event webhook api.
 */
@RestController
@ApiResource(path = "/airagent")
@Slf4j
public class AgentStripeController {

    @Autowired(required = false)
    private StripeTemplate stripeTemplate;

    @Resource
    private IStripeEventService iStripeEventService;

    @Resource
    private IStripeEventHandler iStripeEventHandler;

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    /**
     * retrieve stripe webhook event resource.
     *
     * @param headers  http request headers
     * @param request  http request servlet
     * @param response http response servlet
     * @return string
     */
    @PostResource(path = "/stripe/event", requiredLogin = false)
    @Operation(hidden = true)
    public String retrieveStripeEvent(@RequestHeader HttpHeaders headers,
                                      HttpServletRequest request, HttpServletResponse response) {
        String payload;
        try {
            payload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("fail to get request body", e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "";
        }
        String signature = headers.getFirst(STRIPE_SIGNATURE_HEADER);
        if (StrUtil.isBlank(signature)) {
            log.error("lost request header [Stripe-Signature]");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "";
        }
        Event event;
        try {
            event = stripeTemplate.retrieveEvent(payload, signature);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            log.error("fail to valid payload", e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "";
        } catch (SignatureVerificationException e) {
            // Invalid signature
            log.error("fail to valid stripe signature", e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "";
        }

        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
            // save event to db
            boolean duplicationEvent = iStripeEventService.isExist(event.getId());
            if (!duplicationEvent) {
                iStripeEventService.createEvent(event.getId(), event.getType(),
                    stripeObject.toJson());
            }
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
            log.error("fail to deserialize event data object!");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "";
        }
        // Handle the event
        switch (event.getType()) {
            case "checkout.session.completed": {
                // customer submit checkout success, waiting funds to be transferred from blank account
                Session session = (Session) stripeObject;
                iStripeEventHandler.fulfillOrder(session);
                break;
            }
            case "customer.subscription.created": {
                Subscription subscription = (Subscription) stripeObject;
                iStripeEventHandler.subscriptionCreated(subscription);
                break;
            }
            case "customer.subscription.updated": {
                Subscription subscription = (Subscription) stripeObject;
                iStripeEventHandler.subscriptionUpdated(subscription);
                break;
            }
            case "customer.subscription.deleted": {
                Subscription subscription = (Subscription) stripeObject;
                iStripeEventHandler.subscriptionDeleted(subscription);
                break;
            }
            default:
                log.warn("Unhandled event type: " + event.getType());
        }
        return "";
    }
}
