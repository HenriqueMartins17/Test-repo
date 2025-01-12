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

package com.apitable.enterprise.social.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.apitable.enterprise.social.mapper.SocialWecomPermitOrderMapper;
import com.apitable.enterprise.social.service.ISocialWecomPermitOrderService;
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * WeCom Service Provider Interface License Ordering Information
 * </p>
 */
@Service
public class SocialWecomPermitOrderServiceImpl extends ServiceImpl<SocialWecomPermitOrderMapper, SocialWecomPermitOrderEntity> implements ISocialWecomPermitOrderService {

    @Override
    public SocialWecomPermitOrderEntity getByOrderId(String orderId) {
        return getBaseMapper().selectByOrderId(orderId);
    }

    @Override
    public List<SocialWecomPermitOrderEntity> getByOrderStatuses(String suiteId, String authCorpId, List<Integer> orderStatuses) {
        return getBaseMapper().selectByOrderStatuses(suiteId, authCorpId, orderStatuses);
    }

}
