package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.server.model.PostTrainResult;

/**
 * callback for ai train request.
 */
public interface TrainCallback {

    /**
     * train request callback.
     *
     * @param trainResult train request response data for the callback.
     */
    void callback(PostTrainResult trainResult);
}
