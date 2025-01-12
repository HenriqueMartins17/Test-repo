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

package com.apitable.enterprise.social.redis;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;

public class RedisKey {

    /**
     * DingTalk sync http Distributed lock key
     */
    public static final String DING_TALK_SYNC_HTTP_EVENT_LOCK_KEY = "vikadata:dingtalk:event:lock:{}:{}:{}:{}";

    /**
     * DingTalk template ID--logo key
     */
    public static final String DING_TALK_TEMPLATE_ICON_CACHE = "vikadata:dingtalk:cache:template:icon:{}";

    /**
     * DingTalk isv synchronizing address book
     */
    public static final String SOCIAL_CONTACT_LOCK = "vikadata:social:contact:lock:{}";

    public static final String WECOM_ISV_CONTACT_USER_GET_CACHE = "vikadata:wecom:isv:cache:contact:user_get:{}";

    public static final String WECOM_ISV_CONTACT_DEPART_LIST_CACHE = "vikadata:wecom:isv:cache:contact:depart_list:{}";

    public static final String WECOM_ISV_CONTACT_TAG_GET_CACHE = "vikadata:wecom:isv:cache:contact:tag_get:{}";

    public static final String WECOM_ISV_CONTACT_USER_SIMPLELIST_CACHE = "vikadata:wecom:isv:cache:contact:user_simplelist:{}";

    public static final String WECOM_ISV_MEMBER_NEW_LIST_CACHE = "vikadata:wecom:isv:cache:member:new_list:{}";

    /**
     * social isv event lock
     */
    public static final String SOCIAL_ISV_EVENT_LOCK = "vikadata:social:isv:event:lock:{}:{}";

    /**
     * social isv event lock
     */
    public static final String SOCIAL_ISV_EVENT_PROCESSING = "vikadata:social:isv:event:processing:{}:{}";

    /**
     * Get DingTalk distributed event key
     *
     * @param subscribeId subscriber
     * @param corpId enterprise
     * @param bizId business data id
     * @param bizType business type
     * @return String
     */
    public static String getDingTalkSyncHttpEventLockKey(String subscribeId, String corpId, String bizId,
            Integer bizType) {
        return StrUtil.format(DING_TALK_SYNC_HTTP_EVENT_LOCK_KEY, subscribeId, corpId, bizId, bizType);
    }

    /**
     * Get DingTalk template icon storage key
     *
     * @param templateId template id
     * @return String
     */
    public static String getDingTalkTemplateIconKey(String templateId) {
        return StrUtil.format(DING_TALK_TEMPLATE_ICON_CACHE, templateId);
    }

    /**
     * Get the DingTalk address book synchronization lock
     *
     * @param spaceId space's id
     * @return String
     */
    public static String getSocialContactLockKey(String spaceId) {
        Assert.notBlank(spaceId, "space does not exist");
        return StrUtil.format(SOCIAL_CONTACT_LOCK, spaceId);
    }

    /**
     * Get the cache key of the enterprise WeChat third-party service provider's address book operation
     *
     * @param authCorpId  authorized enterprise id
     * @return cache key
     */
    public static String getWecomIsvContactUserGetKey(String authCorpId) {
        return CharSequenceUtil.format(WECOM_ISV_CONTACT_USER_GET_CACHE, authCorpId);
    }

    /**
     * Get the cache key of the enterprise WeChat third-party service provider's address book operation
     *
     * @param authCorpId authorized enterprise id
     * @return cache key
     */
    public static String getWecomIsvContactDepartListKey(String authCorpId) {
        return CharSequenceUtil.format(WECOM_ISV_CONTACT_DEPART_LIST_CACHE, authCorpId);
    }

    /**
     * Get the cache key of the enterprise WeChat third-party service provider's address book operation
     *
     * @param authCorpId authorized enterprise id
     * @return cache key
     */
    public static String getWecomIsvContactTagGetKey(String authCorpId) {
        return CharSequenceUtil.format(WECOM_ISV_CONTACT_TAG_GET_CACHE, authCorpId);
    }

    /**
     * Get the cache key of the enterprise WeChat third-party service provider's address book operation
     *
     * @param authCorpId authorized enterprise id
     * @return cache key
     */
    public static String getWecomIsvContactUserSimpleListKey(String authCorpId) {
        return CharSequenceUtil.format(WECOM_ISV_CONTACT_USER_SIMPLELIST_CACHE, authCorpId);
    }

    /**
     * Get the cache key of new members of the WeChat third-party service provider's address book
     *
     * @param authCorpId authorized enterprise id
     * @return cache key
     */
    public static String getWecomIsvMemberNewListKey(String authCorpId) {

        return CharSequenceUtil.format(WECOM_ISV_MEMBER_NEW_LIST_CACHE, authCorpId);

    }


    /**
     * social isv event lock
     *
     * @param tenantId tenant's id
     * @param appId application's id
     * @return String
     */
    public static String getSocialIsvEventLockKey(String tenantId, String appId) {
        Assert.notNull(tenantId, "tenant not null");
        Assert.notBlank(appId, "tenant app not null");
        return StrUtil.format(SOCIAL_ISV_EVENT_LOCK, tenantId, appId);
    }

    /**
     * Get social isv event lock
     *
     * @param tenantId tenant's id
     * @param appId application's id
     * @return String
     */
    public static String getSocialIsvEventProcessingKey(String tenantId, String appId) {
        Assert.notNull(tenantId, "tenant not null");
        Assert.notBlank(appId, "tenant app not null");
        return StrUtil.format(SOCIAL_ISV_EVENT_PROCESSING, tenantId, appId);
    }
}
