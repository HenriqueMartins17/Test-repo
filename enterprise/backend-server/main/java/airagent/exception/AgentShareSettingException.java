package com.apitable.enterprise.airagent.exception;

import com.apitable.core.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ai exception.
 */
@Getter
@AllArgsConstructor
public enum AgentShareSettingException implements BaseException {

    AGENT_NOT_SHARED(1701, "agent not shared"),

    AGENT_SHARING_DISABLED(1702, "agent sharing disabled"),

    ;

    private final Integer code;

    private final String message;
}
