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

package com.apitable.enterprise.censor.service.impl;

import jakarta.annotation.Resource;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import lombok.extern.slf4j.Slf4j;

import com.apitable.shared.context.SessionContext;
import com.apitable.enterprise.censor.mapper.ContentCensorReportMapper;
import com.apitable.enterprise.censor.ro.ContentCensorReportRo;
import com.apitable.enterprise.censor.vo.ContentCensorResultVo;
import com.apitable.enterprise.censor.mapper.ContentCensorResultMapper;
import com.apitable.enterprise.censor.service.IContentCensorResultService;
import com.apitable.workspace.service.INodeService;
import com.apitable.workspace.service.INodeShareService;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.censor.entity.ContentCensorReportEntity;
import com.apitable.enterprise.censor.entity.ContentCensorResultEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apitable.base.enums.DatabaseException.EDIT_ERROR;
import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;
import static com.apitable.base.enums.DatabaseException.QUERY_EMPTY_BY_ID;
import static com.apitable.user.enums.UserException.DING_USER_UNKNOWN;

/**
 * <p>
 * Content Censor Result Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class ContentCensorResultServiceImpl
    extends ServiceImpl<ContentCensorResultMapper, ContentCensorResultEntity>
    implements IContentCensorResultService {

    @Resource
    private ContentCensorResultMapper censorResultMapper;

    @Resource
    private ContentCensorReportMapper censorReportMapper;

    @Resource
    private INodeShareService nodeShareService;

    @Resource
    private INodeService nodeService;

    @Override
    public IPage<ContentCensorResultVo> readReports(Integer status,
                                                    Page<ContentCensorResultVo> page) {
        log.info("Query the report information list");
        IPage<ContentCensorResultVo> censorReportList =
            censorResultMapper.getPageByStatus(status, page);
        ExceptionUtil.isNotEmpty(censorReportList.getRecords(), QUERY_EMPTY_BY_ID);
        return censorReportList;
    }

    @Override
    public void createReports(ContentCensorReportRo censorReportRo) {
        log.info("Submit a report");
        String nodeId = censorReportRo.getNodeId();
        ContentCensorResultEntity resultEntity = censorResultMapper.getByNodeId(nodeId);
        // Check if the node is publicly accessible
        nodeShareService.checkNodeHasShare(nodeId);
        // Node processing logic that has been reported
        if (ObjectUtil.isNotNull(resultEntity)) {
            // Accumulate the number of reports, update the audit information, and the status is pending
            resultEntity.setReportNum(resultEntity.getReportNum() + 1);
            resultEntity.setReportResult(0);
            boolean flag = SqlHelper.retBool(censorResultMapper.updateById(resultEntity));
            ExceptionUtil.isTrue(flag, EDIT_ERROR);
        } else {
            // Query whether the node has been reported
            resultEntity = ContentCensorResultEntity.builder()
                .nodeId(nodeId)
                .reportNum(1L)
                .build();
            // Add pending review information
            boolean flag = SqlHelper.retBool(censorResultMapper.insert(resultEntity));
            ExceptionUtil.isTrue(flag, INSERT_ERROR);
        }
        // Add a report record
        ContentCensorReportEntity reportEntity = ContentCensorReportEntity.builder()
            .nodeId(censorReportRo.getNodeId())
            .reportReason(censorReportRo.getReportReason())
            .build();
        boolean flag = SqlHelper.retBool(censorReportMapper.insert(reportEntity));
        ExceptionUtil.isTrue(flag, INSERT_ERROR);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReports(String nodeId, Integer status) {
        log.info("Handling whistleblower information");
        // Query the information in the session
        String auditorUserId = SessionContext.getDingtalkUserId();
        String auditorName = SessionContext.getDingtalkUserName();
        ExceptionUtil.isNotNull(auditorUserId, DING_USER_UNKNOWN);
        // Check whether the node exists
        ContentCensorResultEntity resultEntity = censorResultMapper.getByNodeId(nodeId);
        ExceptionUtil.isNotNull(resultEntity, QUERY_EMPTY_BY_ID);
        // Update audit results
        if (ObjectUtil.isNotNull(resultEntity)) {
            resultEntity.setReportResult(status);
            resultEntity.setAuditorDtName(auditorUserId);
            resultEntity.setAuditorDtUserId(auditorName);
            boolean flag = SqlHelper.retBool(censorResultMapper.updateById(resultEntity));
            ExceptionUtil.isTrue(flag, EDIT_ERROR);
        }
        if (status == 2) {
            status = 0;
        }
        // If banned, disabled nodes are inaccessible
        // If unsealed, the node returns to normal
        nodeService.updateNodeBanStatus(nodeId, status);
    }

}
