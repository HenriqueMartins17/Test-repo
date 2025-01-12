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

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.entity.SocialWecomPermitOrderAccountEntity;

/**
 * <p>
 * WeCom service provider interface license account information
 * </p>
 */
@Mapper
public interface SocialWecomPermitOrderAccountMapper extends BaseMapper<SocialWecomPermitOrderAccountEntity> {

    /**
     * Obtain interface license account information
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param activeCodes Activation code list
     * @return Activate account information
     */
    List<SocialWecomPermitOrderAccountEntity> selectByActiveCodes(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("activeCodes") List<String> activeCodes);

    /**
     * Query the account expired before the specified time
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param expireTime Specified time
     * @return Account expired before the specified time
     */
    List<SocialWecomPermitOrderAccountEntity> selectByExpireTime(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("expireTime") LocalDateTime expireTime);

    /**
     * Query activation code
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param activateStatuses Account activation status.  Query all if it is blank
     * @return All eligible activation codes
     */
    List<String> selectActiveCodes(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("activateStatuses") List<Integer> activateStatuses);

    /**
     * Query activation code
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param activeCodes Activation code list
     * @param activateStatuses Account activation status. Query all if it is blank
     * @return All eligible activation codes
     */
    List<String> selectActiveCodesByActiveCodesAndStatus(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("activeCodes") List<String> activeCodes, @Param("activateStatuses") List<Integer> activateStatuses);

    /**
     * Query WeCom user ID
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param activateStatuses Account activation status. Query all if it is blank
     * @return List of activated WeCom user IDs
     */
    List<String> selectCpUserIdsByStatus(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("activateStatuses") List<Integer> activateStatuses);

    /**
     * Batch change activation status
     *
     * @param suiteId App Suite ID
     * @param authCorpId Authorized enterprise ID
     * @param activeCodes Activation code list
     * @param activeStatus Status after change
     * @return Number of changes
     */
    int updateActiveStatusByActiveCodes(@Param("suiteId") String suiteId, @Param("authCorpId") String authCorpId,
            @Param("activeCodes") List<String> activeCodes, @Param("activeStatus") Integer activeStatus);

}
