package com.apitable.enterprise.social.autoconfigure.wecom;

/**
 * wecom queue constants.
 */
public class WeComQueueConstants {

    /**
     * wecom buffer exchange
     */
    public static final String WECOM_TOPIC_EXCHANGE_BUFFER = "apitable.wecom.buffer";

    /**
     * wecom dead exchange
     */
    public static final String WECOM_TOPIC_EXCHANGE_DEAD = "apitable.wecom.dead";

    /**
     * wecom isv event router
     */
    public static final String WECOM_ISV_EVENT_TOPIC_ROUTING_KEY = "apitable.wecom.isv.event";

    /**
     * wecom isv event buffer queue
     */
    public static final String WECOM_ISV_EVENT_TOPIC_QUEUE_BUFFER =
        "apitable.wecom.isv.event.buffer";

    /**
     * wecom isv event dead queue
     */
    public static final String WECOM_ISV_EVENT_TOPIC_QUEUE_DEAD = "apitable.wecom.isv.event.dead";

    /**
     * wecom isv license permit router
     */
    public static final String WECOM_ISV_PERMIT_TOPIC_ROUTING_KEY = "apitable.wecom.isv.permit";

    /**
     * wecom isv api permit buffer queue
     */
    public static final String WECOM_ISV_PERMIT_TOPIC_QUEUE_BUFFER =
        "apitable.wecom.isv.permit.buffer";

    /**
     * wecom isv permit dead queue
     */
    public static final String WECOM_ISV_PERMIT_TOPIC_QUEUE_DEAD = "apitable.wecom.isv.permit.dead";

    /**
     * social isv event exchange
     */
    public static final String SOCIAL_ISV_EVENT_EXCHANGE = "apitable.social.isv.event.exchange";

    /**
     * wecom isv event queue
     */
    public static final String WECOM_ISV_EVENT_QUEUE = "apitable.wecom.isv.event";


    /**
     * wecom isv event routing key
     */
    public static final String SOCIAL_ISV_WECOM_ROUTING_KEY = "apitable.isv.wecom.#";

    public static final String RABBIT_ARGUMENT_DLX = "x-dead-letter-exchange";

    public static final String RABBIT_ARGUMENT_DLK = "x-dead-letter-routing-key";
}
