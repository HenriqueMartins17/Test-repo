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

package com.apitable.enterprise.vikabilling.service;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.apitable.space.dto.SpaceSubscriptionDto;
import com.apitable.space.vo.SpaceCapacityPageVO;

public interface IBillingCapacityService {


    /**
     * Get space attachment capacity details
     *
     * @param spaceId  space id
     * @param isExpire whether the attachment capacity is invalid
     * @param page     pagination request parameters
     * @return SpaceCapacityPageVO
     */
    @InterceptorIgnore(illegalSql = "true")
    IPage<SpaceCapacityPageVO> getSpaceCapacityDetail(String spaceId, Boolean isExpire, Page page);

    /**
     * Process attachment capacity order information
     *
     * @param spaceSubscriptionDtoIPage additional subscription plan order pagination information
     * @param page                      pagination request parameters
     * @return SpaceCapacityPageVO
     */
    IPage<SpaceCapacityPageVO> handleCapacitySubscription(IPage<SpaceSubscriptionDto> spaceSubscriptionDtoIPage, Page page);

    /**
     * Check if the space station is certified to receive the official accessory capacity reward
     *
     * @param spaceId   space id
     * @return SpaceCapacityPageVO
     */
    SpaceCapacityPageVO checkOfficialGiftCapacity(String spaceId);

}
