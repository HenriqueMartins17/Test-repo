package com.vikadata.migration.service;

import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.entity.NodeVisitRecordEntity;
import com.vikadata.migration.mapper.NodeVisitRecordMapper;
import com.vikadata.migration.repository.NodeRecentlyBrowsedRepository;
import com.vikadata.migration.schema.NodeRecentlyBrowsedSchema;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class NodeVisitRecordService {

    public static final String COLLECTION_NAME = "node_recently_browsed";

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private NodeRecentlyBrowsedRepository repository;

    @Resource
    private NodeVisitRecordMapper mapper;

    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<NodeRecentlyBrowsedSchema> schemas = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(schemas)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<NodeVisitRecordEntity> entities = transform(schemas);
        this.insert(entities);
        NodeRecentlyBrowsedSchema auditSpaceSchema = schemas.get(schemas.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(schemas.size())
            .lastMigrationId(auditSpaceSchema.getId()).build();
    }

    public List<NodeVisitRecordEntity> transform(List<NodeRecentlyBrowsedSchema> auditSpaceList) {
        List<NodeVisitRecordEntity> entities = Lists.newArrayList();
        auditSpaceList.forEach(schema -> {
            String nodeIds = StringUtils.join(schema.getNodeIds(), ',');
            NodeVisitRecordEntity entity = NodeVisitRecordEntity.builder()
                .spaceId(schema.getSpaceId())
                .memberId(schema.getMemberId())
                .nodeType(schema.getNodeType())
                .nodeIds(nodeIds)
                .isDeleted(schema.isDeleted()?1:0)
                .createdAt(schema.getCreatedAt())
                .updatedAt(schema.getUpdatedAt())
                .build();
            entities.add(entity);
        });
        return entities;
    }

    public void insert(List<NodeVisitRecordEntity> entities) {
        entities.forEach(entity -> this.mapper.insert(entity));
    }

    public List<NodeRecentlyBrowsedSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.repository.findAll(PageRequest.of(0, limit, Sort.by("_id"))).getContent();
        }
        return this.repository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
