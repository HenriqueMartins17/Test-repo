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

package com.apitable.enterprise.audit.service;

import com.apitable.enterprise.audit.entity.SpaceAuditEntity;
import com.apitable.enterprise.audit.model.SpaceAuditPageParamDTO;
import com.apitable.enterprise.audit.model.SpaceAuditPageVO;
import com.apitable.shared.util.page.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * space audit service interface.
 */
public interface ISpaceAuditService extends IService<SpaceAuditEntity> {

    /**
     * get spatial audit paging information.
     *
     * @param spaceId space id
     * @param param   param
     * @return SpaceAuditPageVO
     */
    PageInfo<SpaceAuditPageVO> getSpaceAuditPageVO(String spaceId, SpaceAuditPageParamDTO param);

    /**
     * create space audit record.
     *
     * @param entity SpaceAuditEntity
     */
    void createSpaceAuditRecord(SpaceAuditEntity entity);
}
