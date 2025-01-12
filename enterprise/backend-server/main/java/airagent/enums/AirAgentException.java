package com.apitable.enterprise.airagent.enums;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AirAgentException.
 */
@Getter
@AllArgsConstructor
public enum AirAgentException implements BaseException {

    USER_NOT_FOUND(10001, "User not found"),
    AGENT_NOT_FOUND(10002, "Agent not found");

    private final Integer code;

    private final String message;
}
