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

import com.apitable.organization.entity.MemberEntity;
import com.apitable.shared.util.ibatis.ExpandBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Infoflow application information
 * </p>
 */
@Mapper
public interface InfoflowMemberMapper  extends ExpandBaseMapper<MemberEntity> {

    /**
     *
     * fuzzy search member id by member name
     *
     * <p>
     *  only query non-wecom isv members, or the members had modify name in wecom isv space.
     * </p>
     *
     * @param spaceId space id
     * @param likeName keyword
     * @return member id
     */
    List<Long> selectMemberIdsLikeName(@Param("spaceId") String spaceId, @Param("likeName") String likeName);
}
