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

package com.apitable.enterprise.user.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.user.entity.UserLinkEntity;
import com.apitable.shared.cache.bean.AccountLinkDto;
import com.apitable.user.enums.LinkType;

/**
 * <p>
 * Basic - Account Association Table Mapper Interface
 * </p>
 */
public interface UserLinkMapper extends BaseMapper<UserLinkEntity> {

    /**
     * Query user ID
     *
     * @param unionId unionId
     * @param type    Association Type
     * @return userId
     */
    Long selectUserIdByUnionIdAndType(@Param("unionId") String unionId, @Param("type") Integer type);

    /**
     * Query unionId
     *
     * @param userId User ID
     * @param type   Association Type
     * @return unionId
     */
    String selectUnionIdByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    /**
     * Update the nickname and union id associated with WeChat
     *
     * @param nickName nickName
     * @param unionId  unionId
     * @param openId   openId
     * @return Number of modifications
     */
    int updateNickNameAndUnionIdByOpenId(@Param("nickName") String nickName, @Param("unionId") String unionId, @Param("openId") String openId, @Param("type") Integer type);

    /**
     * Get the third party information associated with the account
     *
     * @param userId User ID
     * @return UserLinkVo
     */
    List<AccountLinkDto> selectVoByUserId(@Param("userId") Long userId);

    /**
     * Unbind the account from the third party
     *
     * @param userId User ID
     * @param type   Third party type
     * @return Number of delete
     */
    int deleteByUserIdAndType(@Param("userId") Long userId, @Param("type") Integer type);

    /**
     * Unbind the account from the third party
     *
     * @param userId User ID
     * @return Number of delete
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Batch Delete Records
     *
     * @param unionIds Third party platform user ID
     * @return Number of execution results
     */
    int deleteByUnionIds(@Param("unionIds") List<String> unionIds);

    /**
     * Obtain vika user ID according to union ID and open ID
     *
     * @param unionId Third party platform user unique ID
     * @param openId Third party platform user ID
     * @param type Third party platform type
     * @return User ID
     */
    Long selectUserIdByUnionIdAndOpenIdAndType(@Param("unionId") String unionId, @Param("openId") String openId,
            @Param("type") LinkType type);

    /**
     * Batch delete according to openId
     *
     * @param openIds Unique identification within open applications
     * @param type Third party type
     * @return Delete quantity
     */
    int deleteByOpenIds(@Param("openIds") List<String> openIds, @Param("type") int type);
}
