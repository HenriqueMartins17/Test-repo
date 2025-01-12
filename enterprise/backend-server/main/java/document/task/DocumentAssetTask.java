/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.document.task;

import static com.apitable.core.constants.RedisConstants.GENERAL_STATICS;
import static net.javacrumbs.shedlock.core.LockAssert.assertLocked;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.asset.entity.AssetEntity;
import com.apitable.asset.mapper.AssetMapper;
import com.apitable.enterprise.document.mapper.DocumentMapper;
import com.apitable.enterprise.document.model.DocumentDTO;
import com.apitable.enterprise.document.model.DocumentOperationDTO;
import com.apitable.integration.grpc.DocumentAssetStatisticResult;
import com.apitable.integration.grpc.DocumentAssetStatisticResult.AssetStatisticInfo;
import com.apitable.integration.grpc.DocumentAssetStatisticResult.DocumentAssetStatisticInfo;
import com.apitable.integration.grpc.DocumentAssetStatisticRo;
import com.apitable.integration.grpc.DocumentAssetStatisticRo.DocumentAssetInfo;
import com.apitable.shared.grpc.IGrpcClientService;
import com.apitable.space.dto.SpaceAssetDTO;
import com.apitable.space.entity.SpaceAssetEntity;
import com.apitable.space.mapper.SpaceAssetMapper;
import com.apitable.space.service.ISpaceAssetService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Document asset task class.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "TEST_ENABLED", havingValue = "false", matchIfMissing = true)
public class DocumentAssetTask {

    @Resource
    private DocumentMapper documentMapper;

    @Resource
    private AssetMapper assetMapper;

    @Resource
    private ISpaceAssetService iSpaceAssetService;

    @Resource
    private SpaceAssetMapper spaceAssetMapper;

    @Resource
    private IGrpcClientService iGrpcClientService;

    @Resource
    private RedisTemplate<String, Long> redisTemplate;

    private static final int BATCH_SIZE = 1000;

    /**
     * Document Asset Static cron.
     * cron: 0 30 * * * ?
     * preview execute desc: ****-10-24 00:30:00, ****-10-24 01:30:00, ****-10-24 02:30:00
     */
    @Scheduled(cron = "${DOCUMENT_ASSET_STATIC_CRON:0 30 * * * ?}")
    @SchedulerLock(name = "documentAssetStatic", lockAtMostFor = "9m", lockAtLeastFor = "9m")
    public void documentAssetStatic() {
        assertLocked();
        log.info("Execute Document Asset Static Cron");
        Long beginId = this.getScanBeginIdFromCache();
        if (beginId == null) {
            this.firstExecuteStatic();
            Long maxId = documentMapper.selectDocumentOperationMaxId();
            this.updateScanBeginIdCache(Optional.ofNullable(maxId).orElse(0L));
            return;
        }
        Set<String> allDocumentNames = new HashSet<>();
        while (true) {
            List<DocumentOperationDTO> documents =
                documentMapper.selectDocumentOperationDTOByIdGreaterThan(beginId, BATCH_SIZE);
            if (documents.isEmpty()) {
                return;
            }
            Set<String> documentNames = documents.stream()
                .map(DocumentOperationDTO::getDocumentName)
                .filter(i -> !allDocumentNames.contains(i))
                .collect(Collectors.toSet());
            if (!documentNames.isEmpty()) {
                allDocumentNames.addAll(documentNames);
            }
            this.statistics(documentNames);
            beginId = documents.get(documents.size() - 1).getId();
            this.updateScanBeginIdCache(beginId);
            if (documents.size() < BATCH_SIZE) {
                return;
            }
        }
    }

    private void firstExecuteStatic() {
        Long minId = 0L;
        while (true) {
            List<DocumentDTO> documents =
                documentMapper.selectByIdGreaterThan(minId, BATCH_SIZE);
            if (documents.isEmpty()) {
                return;
            }
            List<String> documentNames = documents.stream()
                .filter(i -> !i.getCreatedAt().isEqual(i.getUpdatedAt()))
                .map(DocumentDTO::getName)
                .collect(Collectors.toList());
            this.statistics(documentNames);
            minId = documents.get(documents.size() - 1).getId();
            if (documents.size() < BATCH_SIZE) {
                return;
            }
        }
    }

    private void statistics(Collection<String> documentNames) {
        if (documentNames.isEmpty()) {
            return;
        }
        List<SpaceAssetDTO> spaceAssets = spaceAssetMapper.selectSpaceAssetDTO(documentNames);
        if (spaceAssets.isEmpty()) {
            return;
        }
        Set<Long> assetIds = spaceAssets.stream()
            .map(SpaceAssetDTO::getAssetId)
            .collect(Collectors.toSet());
        List<AssetEntity> assets = assetMapper.selectBatchIds(assetIds);
        if (assets.isEmpty()) {
            return;
        }
        Map<Long, String> assetIdToFileUrlMap = assets.stream()
            .collect(Collectors.toMap(AssetEntity::getId, AssetEntity::getFileUrl));
        Map<String, List<SpaceAssetDTO>> nodeIdToSpaceAssetMap =
            spaceAssets.stream().collect(Collectors.groupingBy(SpaceAssetDTO::getNodeId));
        List<DocumentAssetInfo> infos = new ArrayList<>();
        for (Entry<String, List<SpaceAssetDTO>> entry : nodeIdToSpaceAssetMap.entrySet()) {
            List<String> fileUrls = entry.getValue().stream()
                .map(SpaceAssetDTO::getAssetId)
                .filter(assetIdToFileUrlMap::containsKey)
                .map(assetIdToFileUrlMap::get)
                .collect(Collectors.toList());
            DocumentAssetInfo info = DocumentAssetInfo.newBuilder()
                .setDocumentName(entry.getKey())
                .addAllFileUrls(fileUrls)
                .build();
            infos.add(info);
        }
        DocumentAssetStatisticRo ro = DocumentAssetStatisticRo.newBuilder()
            .addAllInfos(infos)
            .build();
        DocumentAssetStatisticResult result =
            iGrpcClientService.documentAssetStatistic(ro);
        if (!result.getSuccess()) {
            log.error("document asset statistic grpc method error.\n{}", result);
            return;
        }
        List<SpaceAssetEntity> updateEntities = new ArrayList<>();
        List<Long> delSpaceAssetId = new ArrayList<>();
        for (DocumentAssetStatisticInfo info : result.getData().getInfosList()) {
            String documentName = info.getDocumentName();
            List<SpaceAssetDTO> assetDTOS = nodeIdToSpaceAssetMap.get(documentName);
            Map<String, SpaceAssetDTO> fileUrlToAssetMap = assetDTOS.stream()
                .collect(Collectors.toMap(i -> assetIdToFileUrlMap.get(i.getAssetId()),
                    Function.identity()));
            for (AssetStatisticInfo statisticInfo : info.getAssetInfosList()) {
                SpaceAssetDTO spaceAssetDTO = fileUrlToAssetMap.get(statisticInfo.getFileUrl());
                if (statisticInfo.getCite() == spaceAssetDTO.getCite()) {
                    continue;
                }
                if (statisticInfo.getCite() > 0) {
                    SpaceAssetEntity spaceAsset = SpaceAssetEntity.builder()
                        .id(spaceAssetDTO.getId())
                        .cite(statisticInfo.getCite())
                        .build();
                    updateEntities.add(spaceAsset);
                } else {
                    delSpaceAssetId.add(spaceAssetDTO.getId());
                }
            }
        }
        if (CollUtil.isNotEmpty(updateEntities)) {
            iSpaceAssetService.updateBatchById(updateEntities);
        }
        if (delSpaceAssetId.size() > 0) {
            spaceAssetMapper.deleteBatchByIds(delSpaceAssetId);
        }
    }

    private Long getScanBeginIdFromCache() {
        String key = StrUtil.format(GENERAL_STATICS, "document-asset-cron", "scan-begin-id");
        Number value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return value.longValue();
    }

    private void updateScanBeginIdCache(Long id) {
        String key = StrUtil.format(GENERAL_STATICS, "document-asset-cron", "scan-begin-id");
        redisTemplate.opsForValue().set(key, id, 30, TimeUnit.DAYS);
    }
}

