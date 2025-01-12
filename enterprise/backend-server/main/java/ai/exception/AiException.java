package com.apitable.enterprise.ai.exception;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai exception.
 */
@Getter
@AllArgsConstructor
public enum AiException implements BaseException {

    AI_NOT_FOUND(1601, "ai not found"),
    AI_TYPE_NOT_SET(1601, "ai type is not set"),
    AI_SETTING_NOT_SET(1601, "ai setting is not set"),
    DATA_SOURCE_NOT_FOUND(1602, "data source not found"),
    CONVERSATION_NOT_FOUND(1603, "conversation not found"),
    TRAIN_FAIL(1604, "failed to train"),
    TRAIN_PREDICT_FAIL(1604, "failed to train predict"),
    UPDATE_FEEDBACK_STATE_FAIL(1605, "failed to train predict");

    private final Integer code;

    private final String message;
}
