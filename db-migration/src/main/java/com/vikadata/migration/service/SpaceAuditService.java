package com.vikadata.migration.service;

import cn.hutool.json.JSONObject;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.entity.SpaceAuditEntity;
import com.vikadata.migration.mapper.SpaceAuditMapper;
import com.vikadata.migration.repository.AuditSpaceRepository;
import com.vikadata.migration.schema.AuditSpaceSchema;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class SpaceAuditService {

    public static final String COLLECTION_NAME = "space_audit";

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AuditSpaceRepository auditSpaceRepository;

    @Resource
    private SpaceAuditMapper spaceAuditMapper;

    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<AuditSpaceSchema> auditSpaceList = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(auditSpaceList)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<SpaceAuditEntity> entities = transform(auditSpaceList);
        this.insert(entities);
        AuditSpaceSchema auditSpaceSchema = auditSpaceList.get(auditSpaceList.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(auditSpaceList.size())
            .lastMigrationId(auditSpaceSchema.getId()).build();
    }

    public List<SpaceAuditEntity> transform(List<AuditSpaceSchema> auditSpaceList) {
        List<SpaceAuditEntity> entities = Lists.newArrayList();
        auditSpaceList.forEach(schema -> {
            SpaceAuditEntity entity = SpaceAuditEntity.builder()
                .spaceId(schema.getSpaceId())
                .memberId(schema.getMemberId())
                .memberName(schema.getMemberName())
                .ipAddress(schema.getIpAddress())
                .userAgent(schema.getUserAgent())
                .category(schema.getCategory())
                .action(schema.getAction())
                .info(new JSONObject(schema.getInfo()).toString())
                .createdAt(schema.getCreatedAt())
                .createdBy(schema.getUserId())
                .build();
            entities.add(entity);
        });
        return entities;
    }

    public void insert(List<SpaceAuditEntity> entities) {
        entities.forEach(entity -> this.spaceAuditMapper.insert(entity));
    }

    public List<AuditSpaceSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.auditSpaceRepository.findAll(PageRequest.of(0, limit, Sort.by("_id"))).getContent();
        }
        return this.auditSpaceRepository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
