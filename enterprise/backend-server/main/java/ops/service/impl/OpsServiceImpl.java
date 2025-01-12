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

package com.apitable.enterprise.ops.service.impl;

import static com.apitable.core.constants.RedisConstants.GENERAL_CONFIG;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.asset.service.IAssetService;
import com.apitable.base.enums.SystemConfigType;
import com.apitable.base.service.ISystemConfigService;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ops.ro.OpsWizardRo;
import com.apitable.enterprise.ops.service.IOpsService;
import com.apitable.space.mapper.SpaceAssetMapper;
import com.apitable.template.service.ITemplateService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Product Operation System Service Implement Class.
 * </p>
 */
@Slf4j
@Service
public class OpsServiceImpl implements IOpsService {

    @Resource
    private IAssetService iAssetService;

    @Resource
    private SpaceAssetMapper spaceAssetMapper;

    @Resource
    private ITemplateService iTemplateService;

    @Resource
    private ISystemConfigService iSystemConfigService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${OPS_AUTH_TOKEN:iBV4vxa6Wx6dBdF6D3QqBsxQ}")
    private String authToken;

    @Override
    public void auth(String token) {
        if (!authToken.equals(token)) {
            throw new BusinessException("Authentication failed.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markTemplateAsset(String templateId, Boolean isReversed) {
        // get all node IDs inside the template
        List<String> nodeIds = iTemplateService.getNodeIdsByTemplateId(templateId);
        if (CollUtil.isEmpty(nodeIds)) {
            return;
        }
        // query all resource IDs referenced by a node
        List<Long> assetIds = spaceAssetMapper.selectDistinctAssetIdByNodeIdIn(nodeIds);
        if (CollUtil.isEmpty(assetIds)) {
            return;
        }
        // modify the template state of resource
        List<String> updatedAssetChecksums =
            iAssetService.updateAssetTemplateByIds(assetIds, !isReversed);
        if (CollUtil.isEmpty(updatedAssetChecksums)) {
            return;
        }
        spaceAssetMapper.updateIsTemplateByAssetChecksumIn(!isReversed, updatedAssetChecksums);
    }

    @Override
    public void saveOrUpdateWizard(Long userId, OpsWizardRo ro) {
        log.info("「{}」update wizard config。rollback:{}", userId, ro.getRollback());
        String key = StrUtil.format(GENERAL_CONFIG, "wizards", ro.getLang());
        boolean rollback = BooleanUtil.isTrue(ro.getRollback());
        Object config = redisTemplate.opsForValue().get(key);
        String preKey = key + "-previous";
        if (rollback) {
            Object preConfig = redisTemplate.opsForValue().get(preKey);
            if (preConfig == null || config == null) {
                throw new BusinessException("The configuration of the previous"
                    + " version does not exist, and the rollback fails");
            }
            // The old and new configuration is exchanged,
            // and the original configuration is retained for 14 days for rollback again
            redisTemplate.opsForValue().set(key, preConfig, 7, TimeUnit.DAYS);
            redisTemplate.opsForValue().set(preKey, config, 14, TimeUnit.DAYS);
            // update the data in database
            iSystemConfigService.saveOrUpdate(userId, SystemConfigType.WIZARD_CONFIG,
                ro.getLang(), JSONUtil.toJsonStr(preConfig));
            return;
        }
        String content = ro.getContent();
        if (StrUtil.isBlank(content)) {
            throw new BusinessException("Configuration content cannot be empty");
        }
        redisTemplate.opsForValue().set(key, content, 7, TimeUnit.DAYS);
        // Alternate between old and new,
        // the original configuration is retained for 14 days for rollback
        if (config != null) {
            redisTemplate.opsForValue().set(preKey, config, 14, TimeUnit.DAYS);
        }
        // save or update the data in database
        iSystemConfigService.saveOrUpdate(userId, SystemConfigType.WIZARD_CONFIG,
            ro.getLang(), JSONUtil.toJsonStr(content));
    }
}
