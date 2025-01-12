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

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import me.chanjar.weixin.common.error.WxErrorException;

import com.apitable.enterprise.social.enums.SocialCpIsvMessageProcessStatus;
import com.apitable.enterprise.social.entity.SocialCpIsvEventLogEntity;

/**
 * <p>
 * Third party platform integration - WeCom third-party service provider application authorization notification information record
 * </p>
 */
public interface ISocialCpIsvMessageService extends IService<SocialCpIsvEventLogEntity> {

    /**
     * Get unprocessed message notifications in batches
     *
     * @param size Quantity obtained in batch
     * @return List of unprocessed message notifications
     */
    List<SocialCpIsvEventLogEntity> getUnprocessedList(int size);

    /**
     * Perform relevant operations on the message notifications that have not been processed
     *
     * @param unprocessedInfo Message notifications to process
     * @throws WxErrorException WeCom Interface exception
     */
    void doUnprocessedInfo(SocialCpIsvEventLogEntity unprocessedInfo) throws WxErrorException;

    /**
     * Send unprocessed message notifications to MQ
     *
     * @param unprocessedId Unhandled messages ID
     * @param infoType Message Type
     * @param authCorpId Authorized enterprises ID
     */
    void sendToMq(Long unprocessedId, String infoType, String authCorpId, String suiteId);

    /**
     * update status by id
     * @param id primary key
     * @param status status
     */
    void updateStatusById(Long id, SocialCpIsvMessageProcessStatus status);

}
