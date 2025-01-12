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

package com.apitable.enterprise.censor.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.censor.ro.ContentCensorReportRo;
import com.apitable.enterprise.censor.vo.ContentCensorResultVo;
import com.apitable.enterprise.censor.entity.ContentCensorResultEntity;

/**
 * <p>
 * Content Censor Result Service
 * </p>
 */
public interface IContentCensorResultService extends IService<ContentCensorResultEntity> {


    /**
     * Query the report information list according to the conditions
     *
     * @param page      page params
     * @param status    processing result, 0 unprocessed, 1 banned, 2 normal (unblocked)
     * @return IPage<ContentCensorResultVo>
     */
    IPage<ContentCensorResultVo> readReports(Integer status, Page<ContentCensorResultVo> page);

    /**
     * Submit a report
     *
     * @param censorReportRo report information
     */
    void createReports(ContentCensorReportRo censorReportRo);

    /**
     * Handling whistleblower information
     *
     * @param nodeId node id
     * @param status processing result, 0 unprocessed, 1 banned, 2 normal (unblocked)
     */
    void updateReports(String nodeId, Integer status);
}
