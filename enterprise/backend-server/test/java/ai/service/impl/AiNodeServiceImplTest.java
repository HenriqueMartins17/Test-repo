package com.apitable.enterprise.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import cn.hutool.core.util.IdUtil;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.airagent.model.training.AirtableDataSource;
import com.apitable.enterprise.airagent.model.training.AitableDataSource;
import com.apitable.enterprise.airagent.model.training.DataSources;
import com.apitable.enterprise.airagent.model.training.DatasheetDataSource;
import com.apitable.enterprise.airagent.model.training.FileDataSource;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.workspace.dto.DatasheetSnapshot;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.ro.NodeOpRo;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class AiNodeServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testFirstCreateDataSource() {
        MockData mockData = createMockData();
        String aiNodeId = createAiNode(mockData.getUserSpace().getSpaceId());
        AiTrainingDataSource dataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(mockData.getDatasheetId());
        iAiNodeService.create(aiNodeId, dataSource);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
    }

    @Test
    void testCreateBatch() {
        MockData mockData = createMockData();
        String aiNodeId = createAiNode(mockData.getUserSpace().getSpaceId());
        DataSources dataSources = new DataSources();
        // add airtable data source
        AirtableDataSource airtable = AirtableDataSource.builder()
            .id(Long.toString(IdUtil.getSnowflake(1, 1).nextId()))
            .setting(
                AirtableDataSource.Setting.builder()
                    .apiKey("key")
                    .baseId("baseId")
                    .tableId("tableName")
                    .build()
            )
            .build();
        dataSources.add(airtable);
        // add aitable data source
        AitableDataSource aitable = AitableDataSource.builder()
            .id(Long.toString(IdUtil.getSnowflake(1, 1).nextId()))
            .setting(
                AitableDataSource.Setting.builder()
                    .apiKey("key")
                    .datasheetId("datasheetId")
                    .viewId("viewId")
                    .build()
            )
            .build();
        dataSources.add(aitable);
        // add file data source
        FileDataSource file = FileDataSource.builder()
            .id(Long.toString(IdUtil.getSnowflake(1, 1).nextId()))
            .setting(
                FileDataSource.Setting.builder()
                    .name("file1.pdf")
                    .url("/2023/01/01/file1.pdf")
                    .size(2048L)
                    .numOfCharacters(200L)
                    .build()
            )
            .build();
        dataSources.add(file);
        // add datasheet data source
        DatasheetDataSource datasheet = DatasheetDataSource.builder()
            .id(Long.toString(IdUtil.getSnowflake(1, 1).nextId()))
            .setting(
                DatasheetDataSource.Setting.builder()
                    .datasheetId("datasheetId")
                    .datasheetName("datasheetName")
                    .viewId("viewId")
                    .viewName("viewName")
                    .revision(1L)
                    .rows(100L)
                    .fields(
                        Lists.list(
                            DatasheetSnapshot.Field.builder()
                                .name("field1")
                                .type(1)
                                .build(),
                            DatasheetSnapshot.Field.builder()
                                .name("field2")
                                .type(1)
                                .build()
                        )
                    )
                    .build()
            ).build();
        dataSources.add(datasheet);
        iAiNodeService.createBatch(aiNodeId, dataSources);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
        assertThat(aiNodeEntityList.size()).isEqualTo(dataSources.size());
    }

    @Test
    void testChangeDataSource() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(userSpace.getSpaceId());
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(userSpace.getSpaceId());
        String beforeDatasheetId =
            iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        AiTrainingDataSource beforeDataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(beforeDatasheetId);
        iAiNodeService.create(aiNodeId, beforeDataSource);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
        // change data source
        String afterDatasheetId =
            iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        AiTrainingDataSource afterDataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(afterDatasheetId);
        iAiNodeService.create(aiNodeId, afterDataSource);
        List<AiNodeEntity> nowAiNodeEntities = iAiNodeService.listByAiId(aiNodeId);
        assertThat(nowAiNodeEntities).isNotEmpty();
        assertThat(nowAiNodeEntities.size()).isEqualTo(1);
        AiNodeEntity nodeEntity = nowAiNodeEntities.iterator().next();
        assertThat(nodeEntity.getNodeId()).isEqualTo(afterDatasheetId);
    }

    @Test
    void testListByAiId() {
        String aiNodeId = createAiNodeWithQaType();
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
    }

    @Test
    void testGetLatestVersionByAiId() {
        String aiNodeId = createAiNodeWithQaType();
        Integer version = iAiNodeService.getLatestVersionByAiId(aiNodeId);
        assertThat(version).isNotNull().isEqualTo(1);
    }

    @Test
    void testDeleteByAiId() {
        String aiNodeId = createAiNodeWithQaType();
        iAiNodeService.deleteByAiId(aiNodeId);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isEmpty();
    }
}
