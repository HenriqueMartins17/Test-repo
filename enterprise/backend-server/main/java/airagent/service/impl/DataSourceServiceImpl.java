package com.apitable.enterprise.airagent.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.service.IAiNodeService;
import com.apitable.enterprise.airagent.model.training.AirtableDataSource;
import com.apitable.enterprise.airagent.model.training.AitableDataSource;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParam;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import com.apitable.enterprise.airagent.model.training.DataSourceType;
import com.apitable.enterprise.airagent.model.training.DataSources;
import com.apitable.enterprise.airagent.model.training.DatasheetDataSource;
import com.apitable.enterprise.airagent.model.training.FileDataSource;
import com.apitable.enterprise.airagent.model.training.MaskedAirtableDataSource;
import com.apitable.enterprise.airagent.model.training.MaskedAitableDataSource;
import com.apitable.enterprise.airagent.service.IDataSourceService;
import com.apitable.workspace.dto.DatasheetSnapshot;
import com.apitable.workspace.entity.DatasheetEntity;
import com.apitable.workspace.enums.DataSheetException;
import com.apitable.workspace.service.IDatasheetMetaService;
import com.apitable.workspace.service.IDatasheetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * data source service.
 */
@Service
public class DataSourceServiceImpl implements IDataSourceService {

    @Resource
    private IAiNodeService iAiNodeService;

    @Resource
    private IDatasheetService iDatasheetService;

    @Resource
    private IDatasheetMetaService iDatasheetMetaService;

    @Override
    public boolean hasDataSource(String aiId) {
        long count = iAiNodeService.getCountByAiId(aiId);
        return count > 0;
    }

    @Override
    public DataSources getDataSources(String aiId) {
        DataSources dataSources = new DataSources();
        List<AiNodeEntity> aiNodeEntities = iAiNodeService.listByAiId(aiId);
        if (aiNodeEntities.isEmpty()) {
            return dataSources;
        }
        aiNodeEntities.forEach(entity -> {
            DataSourceType dataSourceType = DataSourceType.of(entity.getType());
            if (dataSourceType == null) {
                return;
            }
            if (dataSourceType.isDatasheet()) {
                DatasheetDataSource datasheetDataSource = loadDatasheet(entity);
                dataSources.add(datasheetDataSource);
            } else if (dataSourceType.isAirtable()) {
                MaskedAirtableDataSource airtableDataSource = loadAirtable(entity);
                dataSources.add(airtableDataSource);
            } else if (dataSourceType.isAitable()) {
                MaskedAitableDataSource aitableDataSource = loadAitable(entity);
                dataSources.add(aitableDataSource);
            } else if (dataSourceType.isFile()) {
                FileDataSource fileDataSource = loadFile(entity);
                dataSources.add(fileDataSource);
            }
        });
        return dataSources;
    }

    @Override
    public void addDataSources(String aiId,
                               DataSourceCreateParams dataSourceCreateParams) {
        if (CollectionUtils.isEmpty(dataSourceCreateParams)) {
            return;
        }
        DataSources dataSources = new DataSources();
        dataSourceCreateParams.forEach(createParam -> {
            DataSourceType dataSourceType = createParam.getType();
            if (dataSourceType.isDatasheet()) {
                DataSourceCreateParam.Datasheet datasheet = createParam.getDatasheet();
                DatasheetDataSource datasheetDataSource = DatasheetDataSource.builder()
                    .setting(
                        buildDatasheetDataSourceSetting(datasheet.getDatasheetId(),
                            datasheet.getViewId())
                    )
                    .build();
                dataSources.add(datasheetDataSource);
            } else if (dataSourceType.isAirtable()) {
                DataSourceCreateParam.Airtable airtable = createParam.getAirtable();
                AirtableDataSource airtableDataSource = AirtableDataSource.builder()
                    .setting(
                        AirtableDataSource.Setting.builder()
                            .apiKey(airtable.getApiKey())
                            .baseId(airtable.getBaseId())
                            .tableId(airtable.getTableId())
                            .build())
                    .build();
                dataSources.add(airtableDataSource);
            } else if (dataSourceType.isAitable()) {
                DataSourceCreateParam.Aitable aitable = createParam.getAitable();
                AitableDataSource aitableDataSource = AitableDataSource.builder()
                    .setting(
                        AitableDataSource.Setting.builder()
                            .apiKey(aitable.getApiKey())
                            .datasheetId(aitable.getDatasheetId())
                            .viewId(aitable.getViewId())
                            .build()
                    )
                    .build();
                dataSources.add(aitableDataSource);
            } else if (dataSourceType.isFile()) {
                DataSourceCreateParam.File file = createParam.getFile();
                FileDataSource fileDataSource = FileDataSource.builder()
                    .setting(
                        FileDataSource.Setting.builder()
                            .name(file.getName())
                            .url(file.getUrl())
                            .build()
                    )
                    .build();
                dataSources.add(fileDataSource);
            }
        });
        iAiNodeService.createBatch(aiId, dataSources);
    }

    @Override
    public void deleteDataSources(String dataSourceId) {
        long count = iAiNodeService.count(
            new QueryWrapper<AiNodeEntity>().eq("id", Long.parseLong(dataSourceId)));
        if (count == 0) {
            return;
        }
        iAiNodeService.removeById(Long.parseLong(dataSourceId));
    }

    private DatasheetDataSource.Setting buildDatasheetDataSourceSetting(String datasheetId,
                                                                        String viewId) {
        DatasheetDataSource.Setting setting = new DatasheetDataSource.Setting();
        DatasheetEntity datasheetEntity = iDatasheetService.getByDstId(datasheetId);
        if (datasheetEntity == null) {
            throw new BusinessException(DataSheetException.DATASHEET_NOT_EXIST);
        }
        setting.setDatasheetId(datasheetId);
        setting.setRevision(datasheetEntity.getRevision());
        DatasheetSnapshot snapshot = iDatasheetMetaService.getMetaByDstId(datasheetId);
        // extract first view id
        DatasheetSnapshot.View view = snapshot.getMeta().getViews()
            .stream()
            .filter(v -> v.getId().equals(viewId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(DataSheetException.VIEW_NOT_EXIST));
        setting.setViewId(viewId);
        setting.setRows((long) view.getRows().size());
        // extract field id list
        List<String> fieldIds = view.extractFieldIds();
        List<DatasheetSnapshot.Field> fields = snapshot.getMeta().extractFields(fieldIds);
        setting.setFields(fields);
        return setting;
    }

    private DatasheetDataSource loadDatasheet(AiNodeEntity entity) {
        DatasheetDataSource.Setting setting =
            DatasheetDataSource.Setting.fromJsonString(entity.getSetting());
        DatasheetEntity datasheetEntity = iDatasheetService.getByDstId(setting.getDatasheetId());
        if (datasheetEntity == null) {
            throw new BusinessException(DataSheetException.DATASHEET_NOT_EXIST);
        }
        setting.setDatasheetName(datasheetEntity.getDstName());
        DatasheetSnapshot snapshot = iDatasheetMetaService.getMetaByDstId(setting.getDatasheetId());
        // extract first view id
        DatasheetSnapshot.View view = snapshot.getMeta().getViews()
            .stream()
            .filter(v -> v.getId().equals(setting.getViewId()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(DataSheetException.VIEW_NOT_EXIST));
        setting.setViewName(view.getName());
        // extract field id list
        List<String> fieldIds = view.extractFieldIds();
        List<DatasheetSnapshot.Field> fields = snapshot.getMeta().extractFields(fieldIds);
        setting.setFields(fields);
        return DatasheetDataSource.builder()
            .id(Long.toString(entity.getId()))
            .setting(setting)
            .build();
    }

    private MaskedAirtableDataSource loadAirtable(AiNodeEntity entity) {
        MaskedAirtableDataSource.Setting setting =
            MaskedAirtableDataSource.Setting.fromJsonString(entity.getSetting());
        return MaskedAirtableDataSource.builder()
            .id(Long.toString(entity.getId()))
            .setting(setting)
            .build();
    }

    private MaskedAitableDataSource loadAitable(AiNodeEntity entity) {
        MaskedAitableDataSource.Setting setting =
            MaskedAitableDataSource.Setting.fromJsonString(entity.getSetting());
        return MaskedAitableDataSource.builder()
            .id(Long.toString(entity.getId()))
            .setting(setting)
            .build();
    }

    private FileDataSource loadFile(AiNodeEntity entity) {
        FileDataSource.Setting setting =
            FileDataSource.Setting.fromJsonString(entity.getSetting());
        return FileDataSource.builder()
            .id(Long.toString(entity.getId()))
            .setting(setting)
            .build();
    }
}
