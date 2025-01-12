package com.apitable.enterprise.ai.service.impl;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.ai.model.QABotSetting;
import com.apitable.enterprise.ai.service.IAiNodeService;
import com.apitable.enterprise.ai.service.IAiTrainingDataSourceService;
import com.apitable.workspace.dto.DatasheetSnapshot;
import com.apitable.workspace.entity.DatasheetEntity;
import com.apitable.workspace.enums.DataSheetException;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.service.IDatasheetMetaService;
import com.apitable.workspace.service.IDatasheetService;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ai data source service implements.
 */
@Service
public class AiTrainingDataSourceServiceImpl implements IAiTrainingDataSourceService {

    @Resource
    private IAiNodeService iAiNodeService;

    @Resource
    private IDatasheetService iDatasheetService;

    @Resource
    private IDatasheetMetaService iDatasheetMetaService;

    @Override
    public AiTrainingDataSource getDataSourceByAiId(String aiId) {
        List<AiNodeEntity> aiNodeEntities = iAiNodeService.listByAiId(aiId);
        if (aiNodeEntities.isEmpty()) {
            return null;
        }
        AiNodeEntity aiNodeEntity = aiNodeEntities.iterator().next();
        DatasheetEntity datasheetEntity = iDatasheetService.getByDstId(aiNodeEntity.getNodeId());
        if (datasheetEntity == null) {
            return null;
        }
        return new AiTrainingDataSource(datasheetEntity.getDstId(), datasheetEntity.getDstName(),
            NodeType.DATASHEET, QABotSetting.fromJsonString(aiNodeEntity.getSetting()));
    }

    @Override
    public AiTrainingDataSource getDataSourceByDatasheetId(String datasheetId) {
        DatasheetEntity datasheetEntity = iDatasheetService.getByDstId(datasheetId);
        if (datasheetEntity == null) {
            throw new BusinessException(DataSheetException.DATASHEET_NOT_EXIST);
        }
        DatasheetSnapshot snapshot = iDatasheetMetaService.getMetaByDstId(datasheetId);
        // extract first view id
        DatasheetSnapshot.View view = snapshot.getMeta().getViews().iterator().next();
        // extract field id list
        List<String> fieldIds = view.getColumns().stream()
            .filter(column -> !column.isHidden())
            .map(DatasheetSnapshot.Column::getFieldId)
            .collect(Collectors.toList());
        List<DatasheetSnapshot.Field> fields = snapshot.getMeta().getFieldMap()
            .values().stream()
            .filter(field -> fieldIds.contains(field.getId()))
            .collect(Collectors.toList());
        QABotSetting setting = QABotSetting.builder()
            .viewId(view.getId())
            .fields(fields)
            .rows((long) view.getRows().size())
            .revision(datasheetEntity.getRevision())
            .build();
        return new AiTrainingDataSource(datasheetEntity.getDstId(), datasheetEntity.getDstName(),
            NodeType.DATASHEET, setting);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDataSource(String aiId, String dataSourceNodeId) {
        AiTrainingDataSource currentDataSource = getDataSourceByAiId(aiId);
        if (currentDataSource != null && dataSourceNodeId.equals(currentDataSource.getNodeId())) {
            // only update data source setting
            updateDataSourceSetting(aiId, dataSourceNodeId);
            return;
        }
        // or save new data source
        AiTrainingDataSource dataSource = getDataSourceByDatasheetId(dataSourceNodeId);
        iAiNodeService.create(aiId, dataSource);
    }

    @Override
    public void updateDataSourceSetting(String aiId, String dataSourceNodeId) {
        // only update data source setting
        AiTrainingDataSource dataSource = getDataSourceByDatasheetId(dataSourceNodeId);
        if (dataSource == null) {
            throw new BusinessException(AiException.DATA_SOURCE_NOT_FOUND);
        }
        List<AiNodeEntity> aiNodeEntities = iAiNodeService.listByAiId(aiId);
        AiNodeEntity sourceEntity = aiNodeEntities.iterator().next();
        AiNodeEntity updateEntity = new AiNodeEntity();
        updateEntity.setId(sourceEntity.getId());
        updateEntity.setSetting(dataSource.getSetting().toJson());
        iAiNodeService.updateById(updateEntity);
    }
}
