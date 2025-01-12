package com.apitable.enterprise.social.autoconfigure.dingtalk;

/**
 * DingTalk Queue Constants.
 */
public class DingTalkQueueConstants {

    /**
     * The queue entered after the message expires, that is the actual consumption queue
     */
    public final static String DING_TALK_ISV_TOPIC_QUEUE_NAME_DEAD =
        "apitable.dingtalk.org.suite.dead";

    /**
     * Messages sent to the queue will expire after a period of time and enter the {@code vikadata.api.dingtalk.org.suite.process}
     * Each message can control its own failure time
     */
    public final static String DING_TALK_ISV_TOPIC_QUEUE_NAME_BUFFER =
        "apitable.dingtalk.org.suite.buffer";

    public final static String DING_TALK_ISV_HIGH_TOPIC = "apitable.org.suite.#";

    /**
     * buffer exchange
     */
    public final static String DING_TALK_TOPIC_EXCHANGE_BUFFER = "apitable.dingtalk.buffer";

    /**
     * dingtalk DLX exchange
     */
    public final static String DING_TALK_TOPIC_EXCHANGE_DEAD = "apitable.dingtalk.dead";

    /**
     * dingtalk isv event queue
     */
    public static final String DINGTALK_ISV_EVENT_QUEUE = "apitable.dingtalk.isv.event";


    /**
     * dingtalk isv event routing key
     */
    public static final String SOCIAL_ISV_DINGTALK_ROUTING_KEY = "apitable.isv.dingtalk.#";
}
