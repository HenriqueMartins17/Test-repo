package com.vikadata.migration.service;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.entity.DatasheetRecordCommentEntity;
import com.vikadata.migration.mapper.DatasheetRecordCommentMapper;
import com.vikadata.migration.repository.DatasheetRecordCommentRepository;
import com.vikadata.migration.schema.DatasheetRecordDataSchema;
import com.vikadata.migration.schema.DatasheetRecordCommentSchema;
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
public class DatasheetRecordCommentService {

    public static final String COLLECTION_NAME = "vika_datasheet_record_comment";

    @Resource
    private DatasheetRecordCommentRepository repository;

    @Resource
    private DatasheetRecordCommentMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<DatasheetRecordCommentSchema> comments = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(comments)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<DatasheetRecordCommentEntity> entities = transform(comments);
        this.mapper.insertBatch(entities);
        DatasheetRecordCommentSchema schema = comments.get(comments.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(comments.size())
            .lastMigrationId(schema.getId()).build();
    }

    public List<DatasheetRecordCommentEntity> transform(List<DatasheetRecordCommentSchema> schemas) {
        List<DatasheetRecordCommentEntity> entities = Lists.newArrayList();
        schemas.forEach(schema -> {
            String commentMsg = null == schema.getCommentMsg()? "{}" : schema.getCommentMsg().toString();
            DatasheetRecordCommentEntity entity = DatasheetRecordCommentEntity.builder()
                .id(IdWorker.getId())
                .dstId(schema.getDatasheetId())
                .recordId(schema.getRecordId())
                .commentId(schema.getCommentId())
                .commentMsg(commentMsg)
                .revision(schema.getRevision())
                .unitId(schema.getUnitId())
                .isDeleted(schema.getIsDeleted())
                .createdAt(schema.getCreatedAt())
                .createdBy(schema.getCreatedBy())
                .updatedAt(schema.getUpdatedAt())
                .updatedBy(schema.getUpdatedBy())
                .build();
            if (null == entity.getCreatedAt()) {
                entity.setCreatedAt(entity.getUpdatedAt());
            }
            entities.add(entity);
        });
        return entities;
    }

    public List<DatasheetRecordCommentSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.repository.findInPage(PageRequest.of(0, limit, Sort.by("_id")));
        }
        return this.repository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
