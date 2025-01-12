package com.apitable.enterprise.ai.model;

import lombok.Data;

/**
 * send message param.
 */
@Data
public class SendMessageParam {

    private String conversationId;
    private String content;
}
