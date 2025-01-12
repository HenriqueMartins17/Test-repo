package com.apitable.enterprise.airagent.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.airagent.entity.AgentEntity;
import com.apitable.enterprise.airagent.model.AgentCreateRO;
import com.apitable.enterprise.airagent.model.AgentUpdateParams;
import com.apitable.enterprise.airagent.model.AiAgent;
import com.apitable.enterprise.airagent.model.SortedAgents;
import com.apitable.enterprise.airagent.model.training.DataSource;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParam;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import com.apitable.enterprise.airagent.model.training.DataSourceType;
import com.apitable.enterprise.airagent.model.training.DataSources;
import com.apitable.enterprise.airagent.model.training.MaskedAirtableDataSource;
import com.apitable.enterprise.airagent.model.training.MaskedAitableDataSource;
import com.apitable.workspace.enums.IdRulePrefixEnum;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class AgentServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetAgentWithNotExist() {
        AiAgent agent = iAgentService.getAgent("not-exist");
        assertThat(agent).isNull();
    }

    @Test
    void testGetAgentWithExist() {
        AgentUserContext userContext = createAgentUser();
        // create agent id
        String agentId = iAgentService.create(userContext.getUserId(), null);

        AiAgent agent = iAgentService.getAgent(agentId);
        assertThat(agent).isNotNull();
        assertThat(agent.getAgentId()).isEqualTo(agentId);

        AiEntity ai = iAiService.getByAiId(agentId);
        assertThat(ai).isNotNull();
        assertThat(ai.getName()).isEqualTo(agent.getAgentName());
    }

    @Test
    void testCheckAgentWithNotExist() {
        assertThatThrownBy(() -> iAgentService.checkAgent("not-exist"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    void testCheckAgentWithExist() {
        AgentUserContext userContext = createAgentUser();
        // create agent id
        String agentId = iAgentService.create(userContext.getUserId(), null);
        assertThatCode(() -> iAgentService.checkAgent(agentId))
            .doesNotThrowAnyException();
    }

    @Test
    void testGetTopAgent() {
        AgentUserContext userContext = createAgentUser();
        // create agent id
        String agentId = iAgentService.create(userContext.getUserId(), null);
        AgentEntity topAgent = iAgentService.getTopAgent(userContext.getUserId());
        assertThat(topAgent).isNotNull();
        assertThat(topAgent.getAgentId()).isEqualTo(agentId);
    }

    @Test
    void testGetAgents() {
        AgentUserContext userContext = createAgentUser();

        // create agent list
        List<AgentEntity> entities = sortedAgentEntities();
        iAgentService.saveBatch(entities);

        SortedAgents agents = iAgentService.getUserAgents(userContext.getUserId());

        assertThat(agents).isNotEmpty()
            .containsSequence(
                new AiAgent("Agent1", null, "Agent1"),
                new AiAgent("Agent2", "Agent1", "Agent2"),
                new AiAgent("Agent3", "Agent2", "Agent3"),
                new AiAgent("Agent4", "Agent3", "Agent4"),
                new AiAgent("Agent5", "Agent4", "Agent5")
            );
    }

    private List<AgentEntity> sortedAgentEntities() {
        return Lists.list(
            agentEntity("Agent3", "Agent2"),
            agentEntity("Agent5", "Agent4"),
            agentEntity("Agent2", "Agent1"),
            agentEntity("Agent4", "Agent3"),
            agentEntity("Agent1", null)
        );
    }

    private AgentEntity agentEntity(String agentId, String preAgentId) {
        return AgentEntity.builder()
            .aiId(agentId)
            .agentId(agentId)
            .preAgentId(preAgentId)
            .agentName(agentId)
            .build();
    }

    @Test
    void testCreateWithNoneParameter() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        assertThat(agentId).isNotNull();
        assertTrue(agentId.startsWith(IdRulePrefixEnum.AIRAGENT.getIdRulePrefixEnum()));

        AiAgent agent = iAgentService.getAgent(agentId);
        assertThat(agent).isNotNull();
        assertThat(agent.getAgentId()).isEqualTo(agentId);

        AiEntity ai = iAiService.getByAiId(agentId);
        assertThat(ai).isNotNull();
        assertThat(ai.getName()).isEqualTo(agent.getAgentName());
    }

    @Test
    void testCreateWithDuplicateName() {
        AgentUserContext userContext = createAgentUser();
        // init name
        AgentCreateRO ro = new AgentCreateRO();
        ro.setName("test");
        String firstAgentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(firstAgentId).isNotNull();
        // create with duplicate name
        String agentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(agentId).isNotNull();
        assertTrue(agentId.startsWith(IdRulePrefixEnum.AIRAGENT.getIdRulePrefixEnum()));
        // check agent list
        SortedAgents agents = iAgentService.getUserAgents(userContext.getUserId());
        assertThat(agents).isNotEmpty()
            .containsSequence(
                new AiAgent(agentId, null, "test 1"),
                new AiAgent(firstAgentId, agentId, "test")
            );

        // check duplicate name
        AiAgent agent = iAgentService.getAgent(agentId);
        assertThat(agent).isNotNull();
        assertThat(agent.getAgentName()).isEqualTo("test 1");

        AiEntity ai = iAiService.getByAiId(agentId);
        assertThat(ai).isNotNull();
        assertThat(ai.getName()).isEqualTo(agent.getAgentName());
    }

    @Test
    void testInsertMiddleAgent() {
        AgentUserContext userContext = createAgentUser();
        // init name
        AgentCreateRO ro = new AgentCreateRO();
        ro.setName("test");
        String firstAgentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(firstAgentId).isNotNull();
        // create with duplicate name
        String agentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(agentId).isNotNull();
        assertTrue(agentId.startsWith(IdRulePrefixEnum.AIRAGENT.getIdRulePrefixEnum()));
        // check agent list
        SortedAgents agents = iAgentService.getUserAgents(userContext.getUserId());
        assertThat(agents).isNotEmpty()
            .containsSequence(
                new AiAgent(agentId, null, "test 1"),
                new AiAgent(firstAgentId, agentId, "test")
            );

        String middleAgentId = iAgentService.create(userContext.getUserId(), AgentCreateRO.builder()
            .preAgentId(agentId)
            .name("test")
            .build());
        assertThat(middleAgentId).isNotNull();
        // check agent list
        SortedAgents latestAgents = iAgentService.getUserAgents(userContext.getUserId());
        assertThat(latestAgents).hasSize(3)
            .extracting(AiAgent::getAgentId)
            .containsExactly(
                agentId,
                middleAgentId,
                firstAgentId
            );
    }

    @Test
    void testInsertLastAgent() {
        AgentUserContext userContext = createAgentUser();
        // init name
        AgentCreateRO ro = new AgentCreateRO();
        ro.setName("test");
        String firstAgentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(firstAgentId).isNotNull();
        // create with duplicate name
        String agentId = iAgentService.create(userContext.getUserId(), ro);
        assertThat(agentId).isNotNull();
        assertTrue(agentId.startsWith(IdRulePrefixEnum.AIRAGENT.getIdRulePrefixEnum()));
        // check agent list
        SortedAgents agents = iAgentService.getUserAgents(userContext.getUserId());
        assertThat(agents).isNotEmpty()
            .containsSequence(
                new AiAgent(agentId, null, "test 1"),
                new AiAgent(firstAgentId, agentId, "test")
            );

        String lastAgentId = iAgentService.create(userContext.getUserId(), AgentCreateRO.builder()
            .preAgentId(firstAgentId)
            .name("test")
            .build());
        assertThat(lastAgentId).isNotNull();
        // check agent list
        SortedAgents latestAgents = iAgentService.getUserAgents(userContext.getUserId());
        assertThat(latestAgents).hasSize(3)
            .extracting(AiAgent::getAgentId)
            .containsExactly(
                agentId,
                firstAgentId,
                lastAgentId
            );
    }

    @Test
    void testMoveAgentOrder() {
        AgentUserContext userContext = createAgentUser();
        AgentCreateRO ro = new AgentCreateRO();
        ro.setName("test");
        String firstAgentId = iAgentService.create(userContext.getUserId(), ro);
        AgentEntity firstAgent = iAgentService.getEntityByAgentId(firstAgentId);
        String preAgentId = "new-pre-agent-id";
        iAgentService.updatePreAgentIdById(firstAgent.getId(), preAgentId);
        AgentEntity updatedAgent = iAgentService.getEntityByAgentId(firstAgentId);
        assertThat(updatedAgent).isNotNull();
        assertThat(updatedAgent.getPreAgentId()).isEqualTo(preAgentId);
    }

    @Test
    void testUpdateName() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        String updateName = "update-name";
        iAgentService.update(agentId, AgentUpdateParams.builder()
            .name(updateName)
            .build());
        AgentEntity updatedAgent = iAgentService.getEntityByAgentId(agentId);
        assertThat(updatedAgent).isNotNull();
        assertThat(updatedAgent.getAgentName()).isEqualTo(updateName);

        AiEntity ai = iAiService.getByAiId(agentId);
        assertThat(ai).isNotNull();
        assertThat(ai.getName()).isEqualTo(updateName);
    }

    @Test
    void testUpdateSetting() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        Map<String, Object> setting = new HashMap<>();
        setting.put("type", "qa");
        setting.put("model", "gpt-4");
        iAgentService.update(agentId, AgentUpdateParams.builder()
            .setting(setting)
            .build());

        AiEntity aiEntity = iAiService.getByAiId(agentId);
        assertThat(aiEntity).isNotNull();
        Map<String, Object> aiSettingMap = JSONUtil.parseObj(aiEntity.getSetting());
        assertThat(aiSettingMap)
            .hasEntrySatisfying("type", value -> assertThat(value).isEqualTo("qa"))
            .hasEntrySatisfying("model", value -> assertThat(value).isEqualTo("gpt-4"));
    }

    @Test
    void testUpdateDataSource() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        DataSourceCreateParams params = buildDataSourceCreateParams();
        iAgentService.update(agentId, AgentUpdateParams.builder()
            .dataSources(params)
            .build());
        DataSources dataSources = iDataSourceService.getDataSources(agentId);
        assertThat(dataSources).isNotEmpty().hasSize(params.size())
            .extracting(DataSource::getType)
            .containsExactly(
                DataSourceType.AIRTABLE,
                DataSourceType.AITABLE,
                DataSourceType.FILE
            );
        assertThat(dataSources)
            .filteredOn(dataSource -> dataSource.getType().isAirtable())
            .isNotEmpty()
            .hasOnlyElementsOfType(MaskedAirtableDataSource.class);
        assertThat(dataSources)
            .filteredOn(dataSource -> dataSource.getType().isAitable())
            .isNotEmpty()
            .hasOnlyElementsOfType(MaskedAitableDataSource.class);
    }

    private DataSourceCreateParams buildDataSourceCreateParams() {
        DataSourceCreateParams params = new DataSourceCreateParams();
        // add airtable data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.AIRTABLE)
                .airtable(
                    DataSourceCreateParam.Airtable.builder()
                        .apiKey("key")
                        .baseId("baseId")
                        .tableId("tableName")
                        .build()
                )
                .build()
        );
        // add aitable data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.AITABLE)
                .aitable(
                    DataSourceCreateParam.Aitable.builder()
                        .apiKey("key")
                        .datasheetId("datasheetId")
                        .viewId("viewId")
                        .build()
                )
                .build()
        );
        // add file data source
        params.add(
            DataSourceCreateParam.builder()
                .type(DataSourceType.FILE)
                .file(
                    DataSourceCreateParam.File.builder()
                        .name("file1.pdf")
                        .url("2023/01/01/file1.pdf")
                        .build()
                )
                .build()
        );
        return params;
    }

    @Test
    void testUpdateAllParam() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        String updateName = "update-name";
        String updateType = "chat";
        String updateModel = "gpt-4";
        Map<String, Object> setting = new HashMap<>();
        setting.put("type", updateType);
        setting.put("model", updateModel);
        setting.put("prompt", "This is prompt");
        setting.put("prologue", "This is prologue");
        DataSourceCreateParams params = buildDataSourceCreateParams();
        iAgentService.update(agentId, AgentUpdateParams.builder()
            .name(updateName)
            .setting(setting)
            .dataSources(params)
            .build());

        AgentEntity updatedAgent = iAgentService.getEntityByAgentId(agentId);
        assertThat(updatedAgent).isNotNull();
        assertThat(updatedAgent.getAgentName()).isEqualTo(updateName);

        AiEntity ai = iAiService.getByAiId(agentId);
        assertThat(ai).isNotNull();
        assertThat(ai.getName()).isEqualTo(updateName);
        assertThat(ai.getType()).isEqualTo(updateType);
        assertThat(ai.getModel()).isEqualTo(updateModel);
        Map<String, Object> aiSettingMap = JSONUtil.parseObj(ai.getSetting());
        assertThat(aiSettingMap)
            .hasEntrySatisfying("type", value -> assertThat(value).isEqualTo("chat"))
            .hasEntrySatisfying("model", value -> assertThat(value).isEqualTo("gpt-4"))
            .hasEntrySatisfying("prompt", value -> assertThat(value).isEqualTo("This is prompt"))
            .hasEntrySatisfying("prologue",
                value -> assertThat(value).isEqualTo("This is prologue"));
        DataSources dataSources = iDataSourceService.getDataSources(agentId);
        assertThat(dataSources).isNotEmpty().hasSize(params.size())
            .extracting(DataSource::getType)
            .containsExactly(
                DataSourceType.AIRTABLE,
                DataSourceType.AITABLE,
                DataSourceType.FILE
            );
        assertThat(dataSources)
            .filteredOn(dataSource -> dataSource.getType().isAirtable())
            .isNotEmpty()
            .hasOnlyElementsOfType(MaskedAirtableDataSource.class);
        assertThat(dataSources)
            .filteredOn(dataSource -> dataSource.getType().isAitable())
            .isNotEmpty()
            .hasOnlyElementsOfType(MaskedAitableDataSource.class);
    }

    @Test
    void testDelete() {
        AgentUserContext userContext = createAgentUser();
        String agentId = iAgentService.create(userContext.getUserId(), null);
        iAgentService.delete(userContext.getUserId(), agentId);
        AgentEntity agentEntity = iAgentService.getEntityByAgentId(agentId);
        assertThat(agentEntity).isNull();
        AiEntity aiEntity = iAiService.getByAiId(agentId);
        assertThat(aiEntity).isNull();
    }
}
