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

import com.apitable.enterprise.social.entity.SocialWecomPermitOrderAccountBindEntity;

/**
 * <p>
 * WeCom service provider interface license account binding information
 * </p>
 */
@Mapper
public interface  SocialWecomPermitOrderAccountBindMapper extends BaseMapper<SocialWecomPermitOrderAccountBindEntity> {

    /**
     * Query activation code
     *
     * @param orderId Interface license order number
     * @return Activation code list
     */
    List<String> selectActiveCodesByOrderId(@Param("orderId") String orderId);

    /**
     * Obtain the number of accounts in the interface license order
     *
     * @param orderId Interface license order number
     * @return Number of accounts
     */
    int selectCountByOrderId(@Param("orderId") String orderId);

}
