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

package com.apitable.enterprise.wechat.service.impl;

import java.util.List;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.wechat.mapper.WechatKeywordReplyMapper;
import com.apitable.enterprise.wechat.service.IWechatMpKeywordReplyService;
import com.apitable.enterprise.wechat.entity.WechatKeywordReplyEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * WeChat Mp Keyword Reply Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class WechatMpKeywordReplyServiceImpl implements IWechatMpKeywordReplyService {

    @Resource
    private WechatKeywordReplyMapper keywordReplyMapper;

    @Override
    public List<WechatKeywordReplyEntity> findRepliesByKeyword(String appId, String keyword) {
        log.info("Query keyword「{}」' reply", keyword);
        return keywordReplyMapper.findRepliesByKeyword(appId, keyword);
    }

}
