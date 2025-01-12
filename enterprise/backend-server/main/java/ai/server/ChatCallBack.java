package com.apitable.enterprise.ai.server;

/**
 * chat call back.
 */
public interface ChatCallBack {

    /**
     * cancel event.
     */
    void onCancel();

    /**
     * on error event handle.
     *
     * @param throwable throwable
     */
    void onError(Throwable throwable);

    /**
     * on complete event handle.
     */
    void onComplete();
}
