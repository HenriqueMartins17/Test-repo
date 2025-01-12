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

import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.social.entity.SocialTenantDomainEntity;
import com.apitable.enterprise.social.enums.TenantDomainStatus;
import com.apitable.enterprise.social.mapper.SocialTenantDomainMapper;
import com.apitable.enterprise.social.model.SpaceBindDomainDTO;
import com.apitable.enterprise.social.service.ISocialTenantDomainService;
import com.apitable.shared.config.properties.ConstProperties;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vikadata.social.wecom.WeComTemplate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Third party platform integration - enterprise tenant exclusive domain name service interface implementation
 * </p>
 */
@Slf4j
@Service
public class SocialTenantDomainServiceImpl
    extends ServiceImpl<SocialTenantDomainMapper, SocialTenantDomainEntity>
    implements ISocialTenantDomainService {

    @Resource
    private ConstProperties constProperties;

    @Autowired(required = false)
    private WeComTemplate weComTemplate;

    @Override
    public void createDomain(String spaceId, String domainPrefix, String domainName) {
        SocialTenantDomainEntity entity = new SocialTenantDomainEntity()
            .setSpaceId(spaceId)
            .setDomainPrefix(domainPrefix)
            .setDomainName(domainName)
            .setStatus(TenantDomainStatus.WAIT_BIND.getCode());
        boolean flag = save(entity);
        ExceptionUtil.isTrue(flag, INSERT_ERROR);
    }

    @Override
    public void enabledDomain(String spaceId) {
        baseMapper.updateStatusBySpaceId(spaceId, TenantDomainStatus.ENABLED.getCode());
    }

    @Override
    public void removeDomain(List<String> spaceIds) {
        List<SocialTenantDomainEntity> entities = baseMapper.selectBySpaceIds(spaceIds);
        if (CollUtil.isNotEmpty(entities)) {
            List<String> existDomainSpace =
                entities.stream().map(SocialTenantDomainEntity::getSpaceId)
                    .collect(Collectors.toList());
            baseMapper.deleteSpaceDomainBySpaceIds(existDomainSpace);
            if (null != weComTemplate) {
                entities.forEach(
                    entity -> weComTemplate.removeEnpDomainName(entity.getDomainPrefix()));
            }
        }
    }

    @Override
    public String getDomainNameBySpaceId(String spaceId, boolean appendHttpsPrefix) {
        // Get the domain name corresponding to the space station
        Map<String, String> spaceDomainToMap =
            this.getSpaceDomainBySpaceIdsToMap(Collections.singletonList(spaceId));
        String domainName =
            spaceDomainToMap.getOrDefault(spaceId, this.getSpaceDefaultDomainName());
        // Add Https prefix or not
        if (appendHttpsPrefix) {
            domainName = StrUtil.prependIfMissingIgnoreCase(domainName, "https://");
        } else {
            domainName = ReUtil.replaceAll(domainName, "http://|https://", StrUtil.EMPTY);
        }
        return domainName;
    }

    @Override
    public String getSpaceDefaultDomainName() {
        return ReUtil.replaceAll(constProperties.getServerDomain(), "http://|https://",
            StrUtil.EMPTY);
    }

    @Override
    public String getSpaceIdByDomainName(String domainName) {
        return baseMapper.selectSpaceIdByDomainName(domainName);
    }

    @Override
    public List<SpaceBindDomainDTO> getSpaceDomainBySpaceIds(List<String> spaceIds) {
        // Clean up unexpected white space characters
        CollUtil.removeBlank(spaceIds);
        if (CollUtil.isEmpty(spaceIds)) {
            return Collections.emptyList();
        }
        List<SpaceBindDomainDTO> result = new ArrayList<>();

        if (CollUtil.isNotEmpty(spaceIds)) {
            // Query the domain bound to the space station list
            List<SpaceBindDomainDTO> dtoList = baseMapper.selectSpaceDomainBySpaceIds(spaceIds);
            Map<String, SpaceBindDomainDTO> dtoTOmap =
                dtoList.stream().collect(Collectors.toMap(SpaceBindDomainDTO::getSpaceId, v -> v));

            for (String spaceId : spaceIds) {
                SpaceBindDomainDTO dto = dtoTOmap.get(spaceId);
                if (null == dto || !TenantDomainStatus.available(dto.getStatus())) {
                    dto = SpaceBindDomainDTO.builder().spaceId(spaceId)
                        .domainName(this.getSpaceDefaultDomainName())
                        .status(TenantDomainStatus.ENABLED.getCode()).build();
                }
                result.add(dto);
            }
        }
        return result;
    }

    @Override
    public Map<String, String> getSpaceDomainBySpaceIdsToMap(List<String> spaceIds) {
        if (CollUtil.isEmpty(spaceIds)) {
            return Collections.emptyMap();
        }
        List<SpaceBindDomainDTO> spaceDomainList = this.getSpaceDomainBySpaceIds(spaceIds);
        return spaceDomainList.stream()
            .collect(Collectors.toMap(SpaceBindDomainDTO::getSpaceId,
                dto -> StrUtil.emptyToDefault(dto.getDomainName(), "")));
    }

    @Override
    public SpaceBindDomainDTO getSpaceDomainByDomainName(String domainName) {
        return baseMapper.selectSpaceDomainByDomainName(domainName);
    }

}
