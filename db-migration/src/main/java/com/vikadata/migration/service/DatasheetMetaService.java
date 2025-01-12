package com.vikadata.migration.service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.google.common.collect.Lists;
import com.vikadata.migration.dto.FieldMapDto;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.dto.ViewMapDto;
import com.vikadata.migration.dto.WidgetPanelDTO;
import com.vikadata.migration.entity.DatasheetEntity;
import com.vikadata.migration.entity.DatasheetMetaEntity;
import com.vikadata.migration.mapper.DatasheetMapper;
import com.vikadata.migration.mapper.DatasheetMetaMapper;
import com.vikadata.migration.repository.DatasheetFieldRepository;
import com.vikadata.migration.repository.DatasheetRepository;
import com.vikadata.migration.repository.DatasheetViewRepository;
import com.vikadata.migration.repository.DatasheetWidgetPanelRepository;
import com.vikadata.migration.schema.DatasheetFieldSchema;
import com.vikadata.migration.schema.DatasheetOperationSchema;
import com.vikadata.migration.schema.DatasheetSchema;
import com.vikadata.migration.schema.DatasheetViewSchema;
import com.vikadata.migration.schema.DatasheetWidgetPanelSchema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class DatasheetMetaService {

    public static final String COLLECTION_NAME = "vika_datasheet_meta";

    @Resource
    private DatasheetRepository repository;

    @Resource
    private DatasheetViewRepository viewRepository;

    @Resource
    private DatasheetFieldRepository fieldRepository;

    @Resource
    private DatasheetWidgetPanelRepository widgetRepository;

    @Resource
    private DatasheetMetaMapper mapper;

    @Transactional(rollbackFor = Exception.class)
    public MigrationOnceDto migrateData(String cursorId, int limit) {
        List<DatasheetSchema> datasheets = this.selectByCursorId(cursorId, limit);
        if (CollectionUtils.isEmpty(datasheets)) {
            return MigrationOnceDto.builder().lastMigrationId(cursorId).migrationCount(0).build();
        }
        List<String> datasheetIds = datasheets.stream().map(datasheet -> datasheet.getDatasheetId())
            .collect(Collectors.toList());
        List<DatasheetViewSchema> views = this.viewRepository.findByDatasheetIds(datasheetIds);
        List<DatasheetFieldSchema> fields = this.fieldRepository.findByDatasheetIds(datasheetIds);
        List<DatasheetWidgetPanelSchema> widgets = this.widgetRepository.findByDatasheetIds(datasheetIds);

        List<DatasheetMetaEntity> entities = transform(datasheets, views, fields, widgets);
        this.mapper.insertBatch(entities);
        DatasheetSchema schema = datasheets.get(datasheets.size() - 1);
        return MigrationOnceDto.builder()
            .migrationCount(datasheets.size())
            .lastMigrationId(schema.getId()).build();
    }

    public List<DatasheetMetaEntity> transform(List<DatasheetSchema> datasheets
        , List<DatasheetViewSchema> viewSchemas, List<DatasheetFieldSchema> fieldSchemas
        , List<DatasheetWidgetPanelSchema> widgetSchemas) {
        List<DatasheetMetaEntity> entities = Lists.newArrayList();
        datasheets.forEach(datasheet -> {
            JSONObject meta = this.transformMeta(datasheet, viewSchemas, fieldSchemas, widgetSchemas);
            DatasheetMetaEntity entity = DatasheetMetaEntity.builder()
                .id(IdWorker.getId())
                .dstId(datasheet.getDatasheetId())
                .metaData(meta.toString())
                .revision(datasheet.getRevision())
                .isDeleted(datasheet.getIsDeleted())
                .createdAt(datasheet.getCreatedAt())
                .createdBy(datasheet.getCreatedBy())
                .updatedAt(datasheet.getCreatedAt())
                .updatedBy(datasheet.getCreatedBy())
                .build();
            entities.add(entity);
        });
        return entities;
    }

    private JSONObject transformMeta(DatasheetSchema datasheet, List<DatasheetViewSchema> viewSchemas, List<DatasheetFieldSchema> fieldSchemas
        , List<DatasheetWidgetPanelSchema> widgetSchemas) {
        String datasheetId = datasheet.getDatasheetId();
        JSONObject meta = new JSONObject();
        List<DatasheetViewSchema> views = viewSchemas.stream().filter(schema -> datasheetId.equals(schema.getDatasheetId()))
            .collect(Collectors.toList());
        JSONArray viewArray = this.transformViews(datasheet.getViewIds(), views);
        meta.set("views", viewArray);
        List<DatasheetFieldSchema> fields = fieldSchemas.stream().filter(schema -> datasheetId.equals(schema.getDatasheetId()))
            .collect(Collectors.toList());
        JSONObject fieldMap = this.transformFields(fields);
        meta.set("fieldMap", fieldMap);
        List<DatasheetWidgetPanelSchema> widgets = widgetSchemas.stream().filter(schema -> datasheetId.equals(schema.getDatasheetId()))
            .collect(Collectors.toList());
        JSONArray widgetArray = this.transformWidgets(widgets);
        meta.set("widgetPanels", widgetArray);
        return meta;
    }

    private JSONArray transformViews(List<String> sortedViewIds, List<DatasheetViewSchema> views) {
        JSONArray viewArray = new JSONArray();
        if (CollectionUtils.isEmpty(sortedViewIds)) {
            return viewArray;
        }
        sortedViewIds.forEach(viewId -> {
            DatasheetViewSchema view = views.stream().filter(schema ->
                viewId.equals(schema.getViewId()) && !schema.getIsDeleted()).findFirst().orElse(null);
            if (null != view) {
                ViewMapDto viewMapDto = ViewMapDto.builder().build();
                BeanUtils.copyProperties(view, viewMapDto);
                JSONObject sortInfo = transformSortInfo(view.getSortInfo());
                viewMapDto.setId(view.getViewId());
                viewMapDto.setSortInfo(sortInfo);
                viewArray.add(JSONUtil.parse(viewMapDto));
            }
        });
        return viewArray;
    }

    private JSONObject transformSortInfo(Object sortInfoObject) {
        if (null == sortInfoObject) {
            return null;
        }
        if (sortInfoObject instanceof JSONArray) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("rules", sortInfoObject);
            return jsonObject;
        } else if (sortInfoObject instanceof JSONObject) {
            return (JSONObject) sortInfoObject;
        } else if (sortInfoObject instanceof LinkedHashMap) {
            JSONObject jsonObject = new JSONObject();
            ((LinkedHashMap<String, ?>) sortInfoObject).forEach((k,v) -> {
                jsonObject.set(k, v);
            });
            return jsonObject;
        } else if (sortInfoObject instanceof ArrayList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("rules", sortInfoObject);
            return jsonObject;
        }
        log.warn("unexpected sortInfo type: "+sortInfoObject.getClass().getName());
        return null;
    }

    private JSONObject transformFields(List<DatasheetFieldSchema> fields) {
        JSONObject fieldMap = new JSONObject();
        fields.forEach(field -> {
            if (!field.getIsDeleted()) {
                FieldMapDto fieldMapDto = FieldMapDto.builder().build();
                BeanUtils.copyProperties(field, fieldMapDto);
                fieldMapDto.setId(field.getFieldId());
                fieldMap.set(field.getFieldId(), JSONUtil.parse(fieldMapDto));
            }
        });
        return fieldMap;
    }

    private JSONArray transformWidgets(List<DatasheetWidgetPanelSchema> widgets) {
        JSONArray widgetArray = new JSONArray();
        widgets.forEach(widget -> {
            if (!widget.getIsDeleted()) {
                JSONObject widgetPanelObj = new JSONObject();
                widgetPanelObj.set("id", widget.getWidgetPanelId());
                widgetPanelObj.set("name", widget.getName());
                widgetPanelObj.set("widgets", widget.getWidgets());
                widgetArray.add(widgetPanelObj);
            }
        });
        return widgetArray;
    }

    public List<DatasheetSchema> selectByCursorId(String cursorId, int limit) {
        if (null == cursorId) {
            return this.repository.findInPage(PageRequest.of(0, limit, Sort.by("_id")));
        }
        return this.repository.findByCursorIdInPage(cursorId, PageRequest.of(0, limit));
    }
}
