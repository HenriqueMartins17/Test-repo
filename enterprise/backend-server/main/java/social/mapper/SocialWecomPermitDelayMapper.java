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

package com.apitable.enterprise.social.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialWecomPermitDelayEntity;

/**
 * <p>
 * WeCom service provider interface permission delay task processing information
 * </p>
 */
@Mapper
public interface SocialWecomPermitDelayMapper extends BaseMapper<SocialWecomPermitDelayEntity> {

    /**
     * Obtain the delayed processing information of the enterprise
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param delayType Delay processing type
     * @param processStatuses Processing status
     * @return Delay processing information
     */
    List<SocialWecomPermitDelayEntity> selectByProcessStatuses(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("delayType") Integer delayType, @Param("processStatuses") List<Integer> processStatuses);

    /**
     * Obtain the delayed processing information of the enterprise
     *
     * @param suiteId App Suite ID
     * @param processStatus Processing status
     * @param limit Data Offset
     * @param skip Quantity returned
     * @return Delay processing information
     */
    List<SocialWecomPermitDelayEntity> selectBySuiteIdAndProcessStatus(@Param("suiteId") String suiteId, @Param("processStatus") Integer processStatus,
            @Param("skip") Integer skip, @Param("limit") Integer limit);

}
