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

package com.apitable.enterprise.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.audit.entity.SpaceAuditEntity;
import com.apitable.enterprise.audit.model.SpaceAuditPageParamDTO;

public interface SpaceAuditMapper extends BaseMapper<SpaceAuditEntity> {

    IPage<SpaceAuditEntity> selectSpaceAuditPage(Page<SpaceAuditEntity> page, @Param("spaceId") String spaceId, @Param("param") SpaceAuditPageParamDTO param);

    int insertEntity(@Param("entity") SpaceAuditEntity entity);
}
