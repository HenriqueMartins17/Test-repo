package com.apitable.enterprise.ai.interfaces.facade;

import static com.apitable.enterprise.ai.server.Inference.getAiSetting;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import cn.hutool.json.JSONUtil;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.interfaces.ai.facade.AiServiceFacade;
import com.apitable.interfaces.ai.model.AiUpdateParam;
import com.apitable.mock.bean.MockUserSpace;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class EnterpriseAiServiceFacadeImplTest extends AbstractApitableSaasIntegrationTest {

    @Resource
    private AiServiceFacade aiServiceFacade;

    @Test
    void testUpdateAiName() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        String aiNodeId = createAiNode(userSpace.getSpaceId());
        String newAiNodeName = "changed ai";
        aiServiceFacade.updateAi(aiNodeId, AiUpdateParam.builder()
            .name(newAiNodeName)
            .build());

        AiEntity aiEntity = iAiService.getByAiId(aiNodeId);
        assertThat(aiEntity).isNotNull();
        assertThat(aiEntity.getName()).isEqualTo(newAiNodeName);
    }

    @Test
    void testUpdateAiSetting() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class);
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            Map<String, Object> dataOfSettingSchema = new HashMap<>(1);
            dataOfSettingSchema.put("model", "gpt-4");
            PureJson settingSchema = new PureJson();
            settingSchema.put("data", dataOfSettingSchema);
            inference.when(() -> getAiSetting(anyString(), anyString())).thenReturn(settingSchema);
            Map<String, Object> setting = new HashMap<>();
            setting.put("type", "chat");
            aiServiceFacade.updateAi(aiNodeId, AiUpdateParam.builder()
                .setting(setting)
                .build());
            AiEntity aiEntity = iAiService.getByAiId(aiNodeId);
            assertThat(aiEntity).isNotNull();
            assertThat(aiEntity.getType()).isNotBlank().isEqualTo("chat");
            assertThat(aiEntity.getModel()).isNotBlank().isEqualTo("gpt-4");
            Map<String, Object> stringObjectMap = JSONUtil.parseObj(aiEntity.getSetting());
            assertThat(stringObjectMap)
                .hasEntrySatisfying("type", value -> assertThat(value).isEqualTo("chat"));
        }
    }

    @Test
    void testUpdateAllParam() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class);
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            String newAiNodeName = "changed ai";
            String newAiType = "chat";
            String defaultAiModel = "gpt-4";
            Map<String, Object> setting = new HashMap<>();
            setting.put("type", newAiType);

            Map<String, Object> dataOfSettingSchema = new HashMap<>(1);
            dataOfSettingSchema.put("model", defaultAiModel);
            PureJson settingSchema = new PureJson();
            settingSchema.put("data", dataOfSettingSchema);
            inference.when(() -> getAiSetting(anyString(), anyString())).thenReturn(settingSchema);
            aiServiceFacade.updateAi(aiNodeId, AiUpdateParam.builder()
                .name(newAiNodeName)
                .setting(setting)
                .build());
            AiEntity aiEntity = iAiService.getByAiId(aiNodeId);
            assertThat(aiEntity).isNotNull();
            assertThat(aiEntity.getName()).isEqualTo(newAiNodeName);
            assertThat(aiEntity.getType()).isEqualTo(newAiType);
            assertThat(aiEntity.getModel()).isEqualTo(defaultAiModel);
            Map<String, Object> stringObjectMap = JSONUtil.parseObj(aiEntity.getSetting());
            assertThat(stringObjectMap)
                .hasEntrySatisfying("type", value -> assertThat(value).isEqualTo("chat"));
        }

    }
}
