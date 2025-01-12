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
 * Third Party System - WeChat Authorization Table
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
@TableName(keepGlobalPrefix = true, value = "wechat_authorization")
public class WechatAuthorizationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Authorizer appid
     */
    private String authorizerAppid;

    /**
     * Interface call token
     */
    private String authorizerAccessToken;

    /**
     * Token validity period, unit: seconds
     */
    private Long accessTokenExpire;

    /**
     * Refresh Token 
     */
    private String authorizerRefreshToken;

    /**
     * Nick Name
     */
    private String nickName;

    /**
     * Avatar
     */
    private String avatar;

    /**
     * Type of official account (0:Subscription No,1:The subscription number upgraded from the old historical account,2:Service No)
     */
    private Integer serviceType;

    /**
     * Official Account/Widget（Only-1/0）Type of certification (-1:Not certified,0:WeChat,1:Sina Weibo,2:Tencent Weibo,3:Qualified but not yet qualified,4:It has passed the qualification certification and has not yet passed the name certification, but has passed the Sina Weibo certification,5:It has passed the qualification certification and has not yet passed the name certification, but has passed the Tencent Weibo certification)
     */
    private Integer verifyType;

    /**
     * Original ID
     */
    private String userName;

    /**
     * Wechat set by official account
     */
    private String alias;

    /**
     * Principal Name
     */
    private String principalName;

    /**
     * Opening status of functions（0 means not opened, 1 means opened）
     */
    private String businessInfo;

    /**
     * URL of QR code image
     */
    private String qrcodeUrl;

    /**
     * Introduction to widget account
     */
    private String signature;

    /**
     * Widget Configuration
     */
    private String miniprograminfo;

    /**
     * Create Time
     */
    private LocalDateTime createdAt;

    /**
     * Update Time
     */
    private LocalDateTime updatedAt;


}
