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

package com.apitable.enterprise.integral.service;

import java.util.Collection;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.apitable.enterprise.integral.enums.IntegralAlterType;
import com.apitable.user.vo.IntegralRecordVO;

public interface IIntegralService {

    /**
     * get the total user integral
     *
     * @param userId    user id
     * @return integral
     */
    int getTotalIntegralValueByUserId(Long userId);

    /**
     * get integral's change records.
     *
     * @param page      page
     * @param userId    user id
     * @return IntegralRecordVO
     */
    IPage<IntegralRecordVO> getIntegralRecordPageByUserId(Page<IntegralRecordVO> page, Long userId);

    /**
     * trigger integral operation
     *
     * @param action    integral action identifier
     * @param alterType alterType
     * @param by        operator
     * @param parameter parameter
     */
    void trigger(String action, IntegralAlterType alterType, Long by, JSONObject parameter);

    /**
     * change integral
     *
     * @param actionCode    integral action identifier
     * @param alterType     alterType
     * @param alterIntegral alterIntegral
     * @param by            operator
     * @param parameter     parameter
     */
    void alterIntegral(String actionCode, IntegralAlterType alterType, int alterIntegral, Long by, JSONObject parameter);

    /**
     * create integral
     *
     * @param userId             userId
     * @param actionCode         integral action identifier
     * @param alterType          alterType
     * @param oldIntegralValue   oldIntegralValue
     * @param alterIntegralValue alterIntegralValue
     * @param parameter          parameter
     * @return record id
     */
    Long createHistory(Long userId, String actionCode, IntegralAlterType alterType, Integer oldIntegralValue, Integer alterIntegralValue, JSONObject parameter);

    /**
     * get the number of participating activities（the number of add integral）
     *
     * @param userId        userId
     * @param actionCode    integral action identifier
     * @return int
     */
    int getCountByUserIdAndActionCode(Long userId, String actionCode);

    /**
     * Whether user have participated in specified activities
     *
     * @param userId        userId
     * @param actionCodes   integral action identifier
     * @return true | false
     */
    boolean checkByUserIdAndActionCodes(Long userId, Collection<String> actionCodes);

    /**
     * activity integral bonus
     *
     * @param processor the operator
     */
    void activityReward(String processor);

    /**
     * official invitation code rewards
     *
     * @param registerUserId    registered user id
     */
    void officialInvitedReward(Long registerUserId);

    /**
     * personal invitation code reward
     *
     * @param registerUserId    registered user id
     * @param registerUserName  registered user nickname
     * @param inviteUserId      inviter user id
     */
    void personalInvitedReward(Long registerUserId, String registerUserName, Long inviteUserId);

    /**
     * use invite code to reward
     * @param userId user id
     * @param inviteCode invite code
     */
    void useInviteCodeReward(Long userId, String inviteCode);

    /**
     * reward for special guide wizard
     * @param userId user id
     * @param wizardId wizard step id
     */
    void rewardWizard(Long userId, String wizardId);

    /**
     * update user nick name in integral reward record metadata param
     * @param userId user id
     * @param nickName user nickname
     */
    void updateInvitationUserNickNameInParams(Long userId, String nickName);
}
