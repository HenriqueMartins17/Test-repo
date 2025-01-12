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

import jakarta.annotation.Resource;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

import com.apitable.base.enums.DatabaseException;
import com.apitable.user.enums.ThirdPartyMemberType;
import com.apitable.enterprise.wechat.enums.WechatEventType;
import com.apitable.enterprise.wechat.mapper.ThirdPartyMemberMapper;
import com.apitable.enterprise.wechat.mapper.WechatMpLogMapper;
import com.apitable.enterprise.wechat.service.IWechatMpLogService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.wechat.entity.WechatMpLogEntity;

import org.springframework.stereotype.Service;

import static com.apitable.shared.constants.WechatConstants.QR_SCENE_PRE;

/**
 * <p>
 * WeChat Mp Log Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class WechatMpLogServiceImpl implements IWechatMpLogService {

    @Resource
    private WechatMpLogMapper wechatMpLogMapper;

    @Resource
    private ThirdPartyMemberMapper thirdPartyMemberMapper;

    @Override
    public void create(String appId, String openid, String unionId, WxMpXmlMessage inMessage) {
        log.info("Save wechat mp log. openid：{}，unionId：{}，inMessage：{}", openid, unionId,
            inMessage);
        String nickName = thirdPartyMemberMapper.selectNickNameByUnionIdAndType(appId, unionId,
            ThirdPartyMemberType.WECHAT_PUBLIC_ACCOUNT.getType());
        WechatMpLogEntity entity = WechatMpLogEntity.builder()
            .appId(appId)
            .openId(openid)
            .unionId(unionId)
            .msgType(inMessage.getMsgType())
            .eventType(inMessage.getEvent())
            .scene(inMessage.getEventKey())
            .extra(JSONUtil.parseObj(inMessage.getAllFieldsMap()).toString())
            .creatorName(nickName)
            .build();
        if (inMessage.getEvent().equalsIgnoreCase(WechatEventType.SUBSCRIBE.name()) &&
            StrUtil.isNotBlank(inMessage.getEventKey())) {
            // Not following the official account Follow the event after scanning the QR code,
            // and truncate the prefix returned by the official
            entity.setScene(inMessage.getEventKey().substring(QR_SCENE_PRE.length()));
        }
        boolean flag = SqlHelper.retBool(wechatMpLogMapper.insert(entity));
        ExceptionUtil.isTrue(flag, DatabaseException.INSERT_ERROR);
    }
}
