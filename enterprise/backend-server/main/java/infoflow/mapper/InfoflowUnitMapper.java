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
package com.apitable.enterprise.infoflow.mapper;

import java.util.List;
import java.util.Map;

import com.apitable.organization.entity.MemberEntity;
import com.apitable.shared.util.ibatis.ExpandBaseMapper;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Infoflow application information
 * </p>
 */
@Mapper
public interface InfoflowUnitMapper extends ExpandBaseMapper<MemberEntity> {

    /**
     * Query unitId by userIds and spaceId
     * @param spaceId
     * @param userIds
     * @return unitIds
     */
    @MapKey("userId")
    Map<Long,Map<String, Long>> selectUnitIdsBySpaceIdAndUserIds(@Param("spaceId") String spaceId, @Param("userIds") List<Long> userIds);
}
