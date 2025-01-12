package com.vikadata.migration.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.dto.OperationMapDto;
import com.vikadata.migration.entity.DatasheetChangesetEntity;
import com.vikadata.migration.mapper.DatasheetChangesetMapper;
import com.vikadata.migration.repository.DatasheetChangesetRepository;
import com.vikadata.migration.repository.DatasheetOperationRepository;
import com.vikadata.migration.schema.DatasheetChangesetSchema;
import com.vikadata.migration.schema.DatasheetOperationSchema;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class DatasheetChangesetService {

    public static final String COLLECTION_NAME = "vika_datasheet_changeset";

    @Resource
    private DatasheetChangesetRepository repository;

    @Resource
    private DatasheetOperationRepository operationRepository;

    @Resource
    private DatasheetChangesetMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<DatasheetChangesetSchema> changesets = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(changesets)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<String> datasheetIds = changesets.stream().map(changeset -> changeset.getDatasheetId())
            .collect(Collectors.toList());
        List<Long> revisions = changesets.stream().map(changeset -> changeset.getRevision())
            .collect(Collectors.toList());
        List<DatasheetOperationSchema> operations = this.operationRepository.findByDatasheetIdInAndRevisionIn(datasheetIds, revisions);
        List<DatasheetChangesetEntity> entities = transform(changesets, operations);
        this.mapper.insertBatch(entities);
        DatasheetChangesetSchema schema = changesets.get(changesets.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(changesets.size())
            .lastMigrationId(schema.getId()).build();
    }

    public List<DatasheetChangesetEntity> transform(List<DatasheetChangesetSchema> schemas, List<DatasheetOperationSchema> operationSchemas) {
        List<DatasheetChangesetEntity> entities = Lists.newArrayList();
        schemas.forEach(schema -> {
            DatasheetOperationSchema operationSchema = operationSchemas.stream()
                .filter(operation -> operation.getDatasheetId().equals(schema.getDatasheetId())
                && operation.getRevision().equals(schema.getRevision())).findFirst().orElse(null);
            String operations = transformOperations(operationSchema);
            DatasheetChangesetEntity entity = DatasheetChangesetEntity.builder()
                .id(IdWorker.getId())
                .dstId(schema.getDatasheetId())
                .messageId(schema.getMessageId())
                .memberId(schema.getMemberId())
                .revision(schema.getRevision())
                .operations(operations)
                .isDeleted(false)
                .createdAt(schema.getCreatedAt())
                .createdBy(schema.getCreatedBy())
                .updatedAt(schema.getCreatedAt())
                .updatedBy(schema.getCreatedBy())
                .build();
            entities.add(entity);
        });
        return entities;
    }

    private String transformOperations(DatasheetOperationSchema operationSchema) {
        if (null == operationSchema) {
            return "";
        }
        JSONArray operations = transformLinkValue(operationSchema.getOperations());
        return operations.toString();
    }

    private JSONArray transformLinkValue(JSONArray operations) {
        operations.forEach(operation -> {
            OperationMapDto dto = JSONUtil.parseObj(operation).toBean(OperationMapDto.class);
            if (CollectionUtils.isEmpty(dto.getActions())) {
                return;
            }
            dto.getActions().forEach(obj -> {
                if (obj instanceof JSONObject) {
                    JSONObject action = (JSONObject)obj;
                    Object od = action.get("od");
                    Object oi = action.get("oi");
                    if(DatasheetRecordService.isLinkValue(od)) {
                        JSONArray odArray = DatasheetRecordService.transformLinkValue((JSONArray) od);
                        action.set("od", odArray);
                    }
                    if(DatasheetRecordService.isLinkValue(oi)) {
                        JSONArray oiArray = DatasheetRecordService.transformLinkValue((JSONArray) oi);
                        action.set("oi", oiArray);
                    }
                }
                else {
                    log.warn("invalid action type: "+obj.getClass().getName());
                }
            });
        });
        return operations;
    }

    public List<DatasheetChangesetSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.repository.findInPage(PageRequest.of(0, limit, Sort.by("_id")));
        }
        return this.repository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
