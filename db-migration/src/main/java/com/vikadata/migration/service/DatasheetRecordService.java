package com.vikadata.migration.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.entity.DatasheetRecordEntity;
import com.vikadata.migration.mapper.DatasheetRecordMapper;
import com.vikadata.migration.repository.DatasheetRecordDataRepository;
import com.vikadata.migration.repository.DatasheetRecordRepository;
import com.vikadata.migration.schema.DatasheetRecordDataSchema;
import com.vikadata.migration.schema.DatasheetRecordSchema;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
public class DatasheetRecordService {

    public static final String COLLECTION_NAME = "vika_datasheet_record";

    @Resource
    private DatasheetRecordRepository datasheetRecordRepository;

    @Resource
    private DatasheetRecordDataRepository datasheetRecordDataRepository;

    @Resource
    private DatasheetRecordMapper datasheetRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<DatasheetRecordSchema> records = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(records)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<String> datasheetIds = records.stream().map(schema -> schema.getDatasheetId())
            .collect(Collectors.toList());
        List<String> recordIds = records.stream().map(schema -> schema.getRecordId())
            .collect(Collectors.toList());

        List<DatasheetRecordDataSchema> recordDataList = datasheetRecordDataRepository
            .findByRecordIds(datasheetIds, recordIds);

        List<DatasheetRecordEntity> entities = transform(records, recordDataList);
        this.datasheetRecordMapper.insertBatch(entities);
        DatasheetRecordSchema schema = records.get(records.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(records.size())
            .lastMigrationId(schema.getId()).build();
    }

    public List<DatasheetRecordEntity> transform(List<DatasheetRecordSchema> records
        , List<DatasheetRecordDataSchema> recordDataList) {
        List<DatasheetRecordEntity> entities = Lists.newArrayList();
        records.forEach(schema -> {
            DatasheetRecordDataSchema recordData = recordDataList.stream().filter(data->
                data.getDatasheetId().equals(schema.getDatasheetId())
                    && data.getRecordId().equals(schema.getRecordId()))
                .findFirst().orElse(null);
            String data = transformData(recordData);
            String recordMeta = schema.getRecordMeta().toString();
            String revisionHistory = String.join(",", schema.getRevisionHistory() );
            List<String> revisionHistoryList =  schema.getRevisionHistory();
            if (revisionHistory.length() > 5000) {
                int length = revisionHistoryList.size();
                revisionHistoryList = revisionHistoryList.subList(length - 500, length - 1);
                revisionHistory = String.join(",", revisionHistoryList);
            }
            DatasheetRecordEntity entity = DatasheetRecordEntity.builder()
                .id(IdWorker.getId())
                .dstId(schema.getDatasheetId())
                .recordId(schema.getRecordId())
                .fieldUpdatedInfo(recordMeta)
                .data(data)
                .revision(schema.getRevision())
                .revisionHistory(revisionHistory)
                .isDeleted(schema.getIsDeleted())
                .createdAt(schema.getCreatedAt())
                .createdBy(schema.getCreatedBy())
                .updatedAt(schema.getUpdatedAt())
                .updatedBy(schema.getUpdatedBy())
                .build();
            entities.add(entity);
        });
        return entities;
    }

    private String transformData(DatasheetRecordDataSchema recordData) {
        if (null == recordData) {
            return "{}";
        }
        JSONObject data = recordData.getData();
        data.keySet().stream().forEach(key -> {
            Object fieldValue = data.get(key);
            if (isLinkValue(fieldValue)) {
                fieldValue = transformLinkValue((JSONArray) fieldValue);
                data.set(key, fieldValue);
            }
        });
        recordData.setData(data);
        return recordData.getData().toString();
    }

    public static boolean isLinkValue(Object fieldValue) {
        if (!(fieldValue instanceof JSONArray)) {
            return false;
        }
        JSONArray jsonArray = (JSONArray)fieldValue;
        if (jsonArray.size() == 0) {
            return false;
        }
        for (Object obj : jsonArray) {
            boolean match = false;
            if (obj instanceof JSONObject) {
                match = ((JSONObject) obj).keySet().contains("recordId");
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    public static JSONArray transformLinkValue(JSONArray fieldValue) {
        JSONArray arr = new JSONArray();
        fieldValue.stream().forEach(obj -> {
            arr.add(((JSONObject) obj).getStr("recordId"));
        });
        return arr;
    }

    public List<DatasheetRecordSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.datasheetRecordRepository.findInPage(PageRequest.of(0, limit, Sort.by("_id")));
        }
        return this.datasheetRecordRepository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
