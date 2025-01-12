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

package com.apitable.enterprise.vcode.service.impl;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.vcode.mapper.VCodeUsageMapper;
import com.apitable.enterprise.vcode.service.IVCodeUsageService;
import com.apitable.enterprise.vcode.dto.VCodeDTO;
import com.apitable.enterprise.vcode.entity.CodeUsageEntity;

import org.springframework.stereotype.Service;

/**
 * <p>
 * VCode Usage Service Implement Class
 * </p>
 */
@Slf4j
@Service
public class VCodeUsageServiceImpl implements IVCodeUsageService {

    @Resource
    private VCodeUsageMapper vCodeUsageMapper;

    @Override
    public void createUsageRecord(Long operator, String name, Integer type, String code) {
        log.info("User「{}」({}) create VCode usage record. Type:{},Code:{}", name, operator, type,
            code);
        CodeUsageEntity usage = CodeUsageEntity.builder()
            .type(type)
            .code(code)
            .operator(operator)
            .operatorName(name)
            .build();
        vCodeUsageMapper.insert(usage);
    }

    @Override
    public VCodeDTO getInvitorUserId(Long userId) {
        return vCodeUsageMapper.selectInvitorUserId(userId);
    }
}
