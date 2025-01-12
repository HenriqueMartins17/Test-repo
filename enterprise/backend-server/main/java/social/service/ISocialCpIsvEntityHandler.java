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

package com.apitable.enterprise.social.service;

import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;
import com.vikadata.social.wecom.constants.WeComIsvMessageType;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application message notification information processing
 * </p>
 */
public interface ISocialCpIsvEntityHandler {

    /**
     * Message Type
     *
     * @return {@link WeComIsvMessageType}
     */
    WeComIsvMessageType type();

    /**
     * Process messages that have not yet been processed
     *
     * @param unprocessed Information to be processed
     * @return Whether data processing is successful
     * @throws WxErrorException WeCom interface exception
     */
    boolean process(SocialCpIsvEventLogEntity unprocessed) throws WxErrorException;

}
