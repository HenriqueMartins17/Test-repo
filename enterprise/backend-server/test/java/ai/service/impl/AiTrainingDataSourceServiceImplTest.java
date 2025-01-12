package com.apitable.enterprise.ai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.ro.NodeOpRo;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AiTrainingDataSourceServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetDataSourceByAiId() {
        String aiId = createAiNodeWithQaType();
        AiTrainingDataSource dataSource = iAiTrainingDataSourceService.getDataSourceByAiId(aiId);
        assertThat(dataSource).isNotNull();
    }

    @Test
    void testGetDataSourceByDatasheetId() {
        MockData mockData = createMockData();
        AiTrainingDataSource dataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(mockData.getDatasheetId());
        assertThat(dataSource).isNotNull();
        assertThat(dataSource.getNodeId()).isEqualTo(mockData.getDatasheetId());
        assertThat(dataSource.getNodeType()).isEqualTo(NodeType.DATASHEET);
        assertThat(dataSource.getNodeName()).isNotBlank();
        assertThat(dataSource.getSetting()).isNotNull();
        assertThat(dataSource.getSetting().getRevision()).isZero();
    }

    @Test
    void testUpdateDataSourceWithSameNodeId() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(userSpace.getSpaceId());
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(userSpace.getSpaceId());
        String datasheetId =
            iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        AiTrainingDataSource dataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(datasheetId);
        iAiNodeService.create(aiNodeId, dataSource);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
        // update data source
        iAiTrainingDataSourceService.updateDataSource(aiNodeId, datasheetId);
        List<AiNodeEntity> nowAiNodeEntities = iAiNodeService.listByAiId(aiNodeId);
        assertThat(nowAiNodeEntities).isNotEmpty().hasSize(1);
        AiNodeEntity nodeEntity = nowAiNodeEntities.iterator().next();
        assertThat(nodeEntity.getNodeId()).isEqualTo(datasheetId);
    }

    @Test
    void testUpdateDataSourceWithDifferenceNodeId() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(userSpace.getSpaceId());
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(userSpace.getSpaceId());
        String datasheetId =
            iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        AiTrainingDataSource dataSource =
            iAiTrainingDataSourceService.getDataSourceByDatasheetId(datasheetId);
        iAiNodeService.create(aiNodeId, dataSource);
        List<AiNodeEntity> aiNodeEntityList = iAiNodeService.listByAiId(aiNodeId);
        assertThat(aiNodeEntityList).isNotEmpty();
        // update data source
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(userSpace.getSpaceId());
        String newDatasheetId =
            iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
                NodeOpRo.builder()
                    .parentId(rootNodeId)
                    .type(NodeType.DATASHEET.getNodeType())
                    .build());
        iAiTrainingDataSourceService.updateDataSource(aiNodeId, newDatasheetId);
        List<AiNodeEntity> nowAiNodeEntities = iAiNodeService.listByAiId(aiNodeId);
        assertThat(nowAiNodeEntities).isNotEmpty().hasSize(1);
        AiNodeEntity nodeEntity = nowAiNodeEntities.iterator().next();
        assertThat(nodeEntity.getNodeId()).isEqualTo(newDatasheetId);
    }
}
