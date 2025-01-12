package com.vikadata.migration.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.entity.DatasheetEntity;
import com.vikadata.migration.entity.NodeEntity;
import com.vikadata.migration.mapper.DatasheetMapper;
import com.vikadata.migration.mapper.NodeMapper;
import com.vikadata.migration.repository.DatasheetRepository;
import com.vikadata.migration.schema.DatasheetSchema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class DatasheetService {

    public static final String COLLECTION_NAME = "vika_datasheet";

    @Resource
    private DatasheetRepository datasheetRepository;

    @Resource
    private DatasheetMapper datasheetMapper;

    @Resource
    private NodeMapper nodeMapper;

    @Transactional(rollbackFor = Exception.class)
    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<DatasheetSchema> datasheetList = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(datasheetList)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<String> datasheetIds = datasheetList.stream().map(schema -> schema.getDatasheetId())
            .collect(Collectors.toList());

        List<NodeEntity> nodes = nodeMapper.selectByNodeIds(datasheetIds);

        List<DatasheetEntity> entities = transform(datasheetList, nodes);
        this.datasheetMapper.insertBatch(entities);
        DatasheetSchema datasheetSchema = datasheetList.get(datasheetList.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(datasheetList.size())
            .lastMigrationId(datasheetSchema.getId()).build();
    }

    public List<DatasheetEntity> transform(List<DatasheetSchema> datasheetList, List<NodeEntity> nodes) {
        List<DatasheetEntity> entities = Lists.newArrayList();
        Map<String, NodeEntity> nodeMap = nodes.stream()
            .collect(Collectors.toMap(NodeEntity::getNodeId, Function.identity()));
        datasheetList.forEach(schema -> {
            Long id = IdWorker.getId();
            NodeEntity nodeEntity = nodeMap.get(schema.getDatasheetId());
            String nodeName = null == nodeEntity? id+"_datasheet" : nodeEntity.getNodeName();
            DatasheetEntity entity = DatasheetEntity.builder()
                .id(id)
                .spaceId(schema.getSpaceId())
                .dstId(schema.getDatasheetId())
                .nodeId(schema.getDatasheetId())
                .dstName(nodeName)
                .revision(schema.getRevision())
                .isDeleted(schema.getIsDeleted())
                .createdAt(schema.getCreatedAt())
                .createdBy(schema.getCreatedBy())
                .updatedAt(schema.getUpdatedAt())
                .updatedBy(schema.getUpdatedBy())
                .build();
            if (null == entity.getIsDeleted()) {
                entity.setIsDeleted(false);
            }
            if (null == entity.getCreatedAt()) {
                entity.setCreatedAt(LocalDateTime.now());
            }
            entities.add(entity);
        });
        return entities;
    }

    public List<DatasheetSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.datasheetRepository.findInPage(PageRequest.of(0, limit, Sort.by("_id")));
        }
        return this.datasheetRepository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
