/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.wechat.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * Third Party System - WeChat Keyword Reply Table
 * </p>
 *
 * @author Mybatis Generator Tool
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
@TableName(keepGlobalPrefix = true, value = "wechat_keyword_reply")
public class WechatKeywordReplyEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Official account Appid（link#xxxx_wechat_authorization#authorizer_appid）
     */
    private String appId;

    /**
     * Rule Name
     */
    private String ruleName;

    /**
     * Keyword matching pattern: contain means that the message contains the keyword, and equal means that the message content must be strictly the same as the keyword
     */
    private String matchMode;

    /**
     * Reply mode: reply_all represents all replies, and random_one represents one random reply
     */
    private String replyMode;

    /**
     * Keywords: for text type, content is the text content; for image, picture, voice and video types, content is the media ID
     */
    private String keyword;

    /**
     * Reply content: for text type, content is text content; for image, image, voice and video type, content is media ID
     */
    private String content;

    /**
     * The type of automatic reply. The types of automatic reply after attention and automatic reply to messages only support text, image, voice, and video. The automatic reply to keywords includes news
     */
    private String type;

    /**
     * Reply content of graphic message
     */
    private String newsInfo;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
