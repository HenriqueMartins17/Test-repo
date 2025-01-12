package com.apitable.enterprise.ai.constants;

/**
 * constant variables.
 *
 * @author Shawn Deng
 */
public class AiConstants {

    /**
     * conversation id header in http header.
     */
    public static final String CONVERSATION_ID_HEADER = "X-Conversation-Id";

    /**
     * training maine exchange name.
     */
    public static final String TRAINING_EXCHANGE_MAIN = "ai";

    /**
     * training delay exchange name.
     */
    public static final String TRAINING_EXCHANGE_MAIN_DELAY = "ai.delay";

    /**
     * training retry exchange name.
     */
    public static final String TRAINING_EXCHANGE_MAIN_RETRY = "ai.retry";

    /**
     * training failed exchange name.
     */
    public static final String TRAINING_EXCHANGE_MAIN_FAILED = "ai.failed";

    /**
     * training main queue name.
     */
    public static final String TRAINING_QUEUE_NAME = "training";

    /**
     * training delay queue name.
     */
    public static final String TRAINING_DELAY_QUEUE_NAME = "training@delay";

    /**
     * training retry queue name.
     */
    public static final String TRAINING_RETRY_QUEUE_NAME = "training@retry";

    /**
     * training failed queue name.
     */
    public static final String TRAINING_FAILED_QUEUE_NAME = "training@failed";

    /**
     * training route key.
     */
    public static final String TRAINING_ROUTE_KEY = "training.#";

    /**
     * training transaction route key.
     */
    public static final String TRAINING_TRANSACTION_ROUTE_KEY = "training.transaction";
}
