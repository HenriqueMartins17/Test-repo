package com.apitable.enterprise.ai.service.impl;

import static com.apitable.enterprise.ai.server.Inference.getAiInfo;
import static com.apitable.enterprise.ai.server.Inference.getAiSetting;
import static com.apitable.shared.util.IdUtil.createNodeId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.model.Ai;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.ai.model.AiUpdateParams;
import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.server.AiServerException;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.AiInfo;
import com.apitable.enterprise.ai.server.model.TrainingInfo;
import com.apitable.enterprise.apitablebilling.interfaces.model.BillingSubscriptionInfo;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.shared.util.IdUtil;
import com.apitable.workspace.enums.NodeType;
import com.apitable.workspace.ro.NodeOpRo;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AiServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testCreate() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiId = createAiNode(userSpace.getSpaceId());
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        assertThat(aiEntity).isNotNull();
        assertThat(aiEntity.getAiId()).isEqualTo(aiId);
    }

    @Test
    void testCreateAiNode() {
        MockData mockData = createMockData();
        try (
            MockedStatic<IdUtil> idUtil = mockStatic(IdUtil.class)
        ) {
            String mockAiId = "ai_xxxxxxxxxx";
            idUtil.when(() -> createNodeId(NodeType.AI_CHAT_BOT))
                .thenReturn(mockAiId);
            doReturn(new BillingSubscriptionInfo()).when(
                entitlementServiceFacade).getSpaceSubscription(anyString());
            String aiNodeId = iNodeService.createNode(mockData.getUserSpace().getUserId(),
                mockData.getUserSpace().getSpaceId(),
                NodeOpRo.builder()
                    .parentId(mockData.getRootNodeId())
                    .type(NodeType.AI_CHAT_BOT.getNodeType())
                    .build());
            assertThat(aiNodeId).isNotBlank().isEqualTo(mockAiId);
        }
    }

    @Test
    void testGetAiWithCallServerFail() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            inference.when(() -> getAiInfo(aiNodeId))
                .thenThrow(new AiServerException("fail to call ai server"));
            inference.when(() -> getAiSetting(anyString())).thenReturn(null);
            doReturn(new BillingSubscriptionInfo()).when(
                entitlementServiceFacade).getSpaceSubscription(anyString());
            assertThatThrownBy(() -> iAiService.getAi(aiNodeId, null))
                .isInstanceOf(AiServerException.class);
        }
    }

    @Test
    void testGetAiWithCallAiInfoNotExist() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            inference.when(() -> getAiSetting(anyString())).thenReturn(null);
            inference.when(() -> getAiInfo(anyString())).thenReturn(null);
            doReturn(new BillingSubscriptionInfo()).when(
                entitlementServiceFacade).getSpaceSubscription(anyString());
            Ai ai = iAiService.getAi(aiNodeId, null);
            assertThat(ai).isNotNull();
            assertThat(ai.getId()).isEqualTo(aiNodeId);
            assertThat(ai.getCurrentConversationId()).isNull();
            assertThat(ai.getCurrentConversationCreatedAt()).isNull();
            assertThat(ai.getIsTrained()).isFalse();
            assertThat(ai.getSetting()).isNotNull();
        }
    }

    @Test
    void testGetAiWithAiIsTraining() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            AiInfo aiInfo = new AiInfo();
            String mockTrainingId = UUID.randomUUID().toString();
            aiInfo.setLockingTrainingId(mockTrainingId);
            TrainingInfo lockingTrainingInfo = new TrainingInfo();
            lockingTrainingInfo.setTrainingId(mockTrainingId);
            lockingTrainingInfo.setStatus(TrainingStatus.TRAINING);
            aiInfo.setLockingTrainingInfo(lockingTrainingInfo);
            inference.when(() -> getAiInfo(aiNodeId)).thenReturn(aiInfo);
            inference.when(() -> getAiSetting(anyString())).thenReturn(null);
            doReturn(new BillingSubscriptionInfo()).when(
                entitlementServiceFacade).getSpaceSubscription(anyString());
            Ai ai = iAiService.getAi(aiNodeId, null);
            assertThat(ai).isNotNull();
            assertThat(ai.getId()).isEqualTo(aiNodeId);
            assertThat(ai.getLatestTrainingId()).isEqualTo(mockTrainingId);
            assertThat(ai.getLatestTrainingStatus()).isEqualTo(TrainingStatus.TRAINING);
            assertThat(ai.getIsTrained()).isFalse();
        }
    }

    @Test
    void getAiInfoByAnonymous() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());

            AiInfo aiInfo = new AiInfo();
            String mockTrainingId = UUID.randomUUID().toString();
            aiInfo.setCurrentTrainingId(mockTrainingId);
            TrainingInfo currentTrainingInfo = new TrainingInfo();
            currentTrainingInfo.setTrainingId(mockTrainingId);
            currentTrainingInfo.setStatus(TrainingStatus.SUCCESS);
            currentTrainingInfo.setStartedAt(RandomUtil.randomLong(10));
            currentTrainingInfo.setFinishedAt(RandomUtil.randomLong(10));
            aiInfo.setCurrentTrainingInfo(currentTrainingInfo);
            aiInfo.setSuccessTrainHistory(Collections.singletonList(mockTrainingId));
            inference.when(() -> getAiInfo(aiNodeId)).thenReturn(aiInfo);
            inference.when(() -> getAiSetting(anyString())).thenReturn(null);
            doReturn(new BillingSubscriptionInfo()).when(
                entitlementServiceFacade).getSpaceSubscription(anyString());
            Ai ai = iAiService.getAi(aiNodeId, null);
            assertThat(ai).isNotNull();
            assertThat(ai.getId()).isEqualTo(aiNodeId);
            assertThat(ai.getLatestTrainingId()).isEqualTo(mockTrainingId);
            assertThat(ai.getLatestTrainingStatus()).isEqualTo(TrainingStatus.SUCCESS);
            assertThat(ai.getLatestTrainingCompletedAt()).isNotNull();
            assertThat(ai.getIsTrained()).isTrue();
        }
    }

    @Test
    void testUpdateAiOnlyType() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
            AiObject aiObject = new AiObject();
            aiObject.setSpaceId(userSpace.getSpaceId());
            aiObject.setAiId(aiNodeId);
            aiObject.setName(DEFAULT_AI_NODE_NAME);
            iAiService.create(aiObject);
            AiEntity beforeAi = iAiService.getByAiId(aiNodeId);
            assertThat(beforeAi).isNotNull();
            PureJson aiSettingSchema = new PureJson();
            aiSettingSchema.put("data", Maps.newHashMap("model", "gpt-4"));
            inference.when(() -> getAiSetting(anyString(), anyString()))
                .thenReturn(aiSettingSchema);
            iAiService.updateAi(aiNodeId, AiUpdateParams.builder()
                .setting(Maps.newHashMap("type", "chat"))
                .build());
            AiEntity afterAi = iAiService.getByAiId(aiNodeId);
            assertThat(afterAi).isNotNull();
            assertThat(afterAi.getType()).isEqualTo("chat");
            assertThat(afterAi.getModel()).isEqualTo("gpt-4");
        }
    }

    @Test
    void testUpdateAiOnlyDescription() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(userSpace.getSpaceId());
        aiObject.setAiId(aiNodeId);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        aiObject.setDescription("simple description");
        iAiService.create(aiObject);
        AiEntity beforeAi = iAiService.getByAiId(aiNodeId);
        assertThat(beforeAi).isNotNull();
        assertThat(beforeAi.getDescription()).isEqualTo("simple description");
        iAiService.updateAi(aiNodeId, AiUpdateParams.builder()
            .description("this a simple description")
            .build());
        AiEntity afterAi = iAiService.getByAiId(aiNodeId);
        assertThat(afterAi).isNotNull();
        assertThat(afterAi.getDescription()).isEqualTo("this a simple description");
    }

    @Test
    void testUpdateAiOnlySetting() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(userSpace.getSpaceId());
        aiObject.setAiId(aiNodeId);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        Map<String, Object> initSetting = Maps.newHashMap("mode", "wizard");
        initSetting.put("isEnabledPromptBox", false);
        initSetting.put("isEnabledPromptTips", false);
        aiObject.setSetting(JSONUtil.toJsonStr(initSetting));
        iAiService.create(aiObject);
        AiEntity beforeAi = iAiService.getByAiId(aiNodeId);
        assertThat(beforeAi).isNotNull();
        Map<String, Object> oldSetting = JSONUtil.parseObj(beforeAi.getSetting());
        assertThat(oldSetting.get("mode")).isEqualTo("wizard");
        assertThat(oldSetting.get("isEnabledPromptBox")).isEqualTo(false);
        assertThat(oldSetting.get("isEnabledPromptTips")).isEqualTo(false);

        // update
        Map<String, Object> setting = Maps.newHashMap("mode", "wizard");
        setting.put("isEnabledPromptBox", true);
        setting.put("isEnabledPromptTips", false);
        iAiService.updateAi(aiNodeId, AiUpdateParams.builder()
            .setting(setting)
            .build());
        AiEntity afterAi = iAiService.getByAiId(aiNodeId);
        assertThat(afterAi).isNotNull();
        assertThat(afterAi.getSetting()).isNotNull().isNotBlank();
        Map<String, Object> parsedJson = JSONUtil.parseObj(afterAi.getSetting());
        assertThat(parsedJson.get("mode")).isEqualTo("wizard");
        assertThat(parsedJson.get("isEnabledPromptBox")).isEqualTo(true);
        assertThat(parsedJson.get("isEnabledPromptTips")).isEqualTo(false);
    }

    @Test
    void testUpdateAiOnlyDataSource() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createNodeId(NodeType.AI_CHAT_BOT);
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(userSpace.getSpaceId());
        aiObject.setAiId(aiNodeId);
        aiObject.setName(DEFAULT_AI_NODE_NAME);
        iAiService.create(aiObject);
        AiEntity beforeAi = iAiService.getByAiId(aiNodeId);
        assertThat(beforeAi).isNotNull();
        AiTrainingDataSource dataSource =
            iAiTrainingDataSourceService.getDataSourceByAiId(aiNodeId);
        assertThat(dataSource).isNull();
        // set data source
        String rootNodeId = iNodeService.getRootNodeIdBySpaceId(userSpace.getSpaceId());
        doReturn(new BillingSubscriptionInfo())
            .when(entitlementServiceFacade).getSpaceSubscription(userSpace.getSpaceId());
        String datasheetId = iNodeService.createNode(userSpace.getUserId(), userSpace.getSpaceId(),
            NodeOpRo.builder()
                .parentId(rootNodeId)
                .type(NodeType.DATASHEET.getNodeType())
                .build());
        AiUpdateParams.DataSourceParam dataSourceParam = new AiUpdateParams.DataSourceParam();
        dataSourceParam.setNodeId(datasheetId);
        iAiService.updateAi(aiNodeId, AiUpdateParams.builder()
            .dataSources(Collections.singletonList(dataSourceParam))
            .build());
        AiEntity afterAi = iAiService.getByAiId(aiNodeId);
        assertThat(afterAi).isNotNull();
        AiTrainingDataSource updatedDataSource =
            iAiTrainingDataSourceService.getDataSourceByAiId(aiNodeId);
        assertThat(updatedDataSource).isNotNull();
        assertThat(updatedDataSource.getNodeId()).isEqualTo(datasheetId);
    }

    @Test
    void testGetLatestTrainingStatus() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class)
        ) {
            inference.when(() -> getAiInfo(anyString())).thenReturn(null);
            TrainingStatus status = iAiService.getLatestTrainingStatus(aiNodeId);
            assertThat(status).isNull();
        }
    }
}
