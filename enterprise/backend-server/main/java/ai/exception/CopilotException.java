package com.apitable.enterprise.ai.exception;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai exception.
 */
@Getter
@AllArgsConstructor
public enum CopilotException implements BaseException {

    COPILOT_NOT_FOUND(1801, "copilot not found"),
    COPILOT_CONVERSATION_NOT_FOUND(1803, "conversation not found")
    ;

    private final Integer code;

    private final String message;
}
