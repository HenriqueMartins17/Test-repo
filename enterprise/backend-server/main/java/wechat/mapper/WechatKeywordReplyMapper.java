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

package com.apitable.enterprise.wechat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.wechat.entity.WechatKeywordReplyEntity;

/**
 * <p>
 * WeChat Keyword Reply Mapper
 * </p>
 */
public interface WechatKeywordReplyMapper {

    /**
     * Clean up all keyword replies
     */
    Integer deleteKeywordReplies(@Param("appId") String appId);

    /**
     * Batch insert
     */
    Integer insertBatchWechatKeywordReply(@Param("appId") String appId, @Param("list") List<WechatKeywordReplyEntity> list);

    /**
     * Query keyword replies
     */
    List<WechatKeywordReplyEntity> findRepliesByKeyword(@Param("appId") String appId, @Param("keyword") String keyword);
}
