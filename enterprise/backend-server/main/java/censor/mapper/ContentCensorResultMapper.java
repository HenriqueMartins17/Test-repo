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

package com.apitable.enterprise.censor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.censor.entity.ContentCensorResultEntity;
import com.apitable.enterprise.censor.vo.ContentCensorResultVo;


/**
 * <p>
 * Content Censor Result Mapper
 * </p>
 */
public interface ContentCensorResultMapper extends BaseMapper<ContentCensorResultEntity> {

    /**
     * Query the report information list
     *
     * @param status    processing result, 0 unprocessed, 1 banned, 2 normal (unblocked)
     * @param page      page params
     * @return List<ContentCensorResultVo>
     */
    IPage<ContentCensorResultVo> getPageByStatus(@Param("status") Integer status, Page<ContentCensorResultVo> page);


    /**
     * Check whether the node has been reported
     *
     * @param nodeId node id
     * @return ContentCensorResultEntity
     */
    ContentCensorResultEntity getByNodeId(@Param("nodeId") String nodeId);
}
