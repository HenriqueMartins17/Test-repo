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

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.ThirdPartyMemberEntity;
import com.apitable.enterprise.wechat.dto.ThirdPartyMemberInfo;
import com.apitable.enterprise.wechat.dto.WechatMemberDto;

/**
 * <p>
 * Third party system - Mapper interface of member information table
 * </p>
 */
public interface ThirdPartyMemberMapper extends BaseMapper<ThirdPartyMemberEntity> {

    /**
     * Query unionId
     *
     * @param appId  appId
     * @param openId openId
     * @param type   Type
     * @return unionId
     */
    String selectUnionIdByOpenIdAndType(@Param("appId") String appId, @Param("openId") String openId, @Param("type") Integer type);

    /**
     * Query nickname
     *
     * @param appId   appId
     * @param unionId unionId
     * @param type    Type
     * @return nickName
     */
    String selectNickNameByUnionIdAndType(@Param("appId") String appId, @Param("unionId") String unionId, @Param("type") Integer type);

    /**
     * Query extra
     *
     * @param id Member ID
     * @return extra
     */
    String selectExtraById(@Param("id") Long id);

    /**
     * Query session key
     *
     * @param id Member ID
     * @return session_key
     */
    String selectSessionKeyById(@Param("id") Long id);

    /**
     * Query member information
     *
     * @param appId   appId
     * @param unionId unionId
     * @param type    Type
     * @return info
     */
    ThirdPartyMemberInfo selectInfo(@Param("appId") String appId, @Param("unionId") String unionId, @Param("type") Integer type);

    /**
     * Get the bind user ID
     *
     * @param id       Member ID
     * @param linkType Associated Third Party Type
     * @return userId
     */
    Long selectUserIdByIdAndLinkType(@Param("id") Long id, @Param("linkType") Integer linkType);

    /**
     * Query WeChat member information
     *
     * @param type   Type
     * @param appId  appId
     * @param openId openId
     * @return Dto
     */
    WechatMemberDto selectWechatMemberDto(@Param("type") Integer type, @Param("appId") String appId, @Param("openId") String openId);

    /**
     * Query user ID and associated WeChat member information
     *
     * @param appId  appId
     * @param mobile phone number
     * @return Dto
     */
    WechatMemberDto selectUserLinkedWechatMemberDto(@Param("appId") String appId, @Param("mobile") String mobile);
}
