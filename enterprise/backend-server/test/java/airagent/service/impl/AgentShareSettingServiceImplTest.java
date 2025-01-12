package com.apitable.enterprise.airagent.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.airagent.exception.AgentShareSettingException;
import com.apitable.enterprise.airagent.model.ShareVO;
import com.apitable.enterprise.airagent.service.IAgentShareSettingService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

public class AgentShareSettingServiceImplTest extends AbstractApitableSaasIntegrationTest {
    @Resource
    private IAgentShareSettingService iAgentShareSettingService;

    @Test
    void testPublishAgentFirst() {
        String shareId = iAgentShareSettingService.publishSharing("ag_test_id");
        assertThat(shareId).isNotNull();
    }

    @Test
    void testPublishAgentAgain() {
        String shareId = iAgentShareSettingService.publishSharing("ag_test_id");
        String secondShareId = iAgentShareSettingService.publishSharing("ag_test_id");
        assertThat(shareId).isEqualTo(secondShareId);
    }

    @Test
    void testCloseShare() {
        String shareId = iAgentShareSettingService.publishSharing("ag_test_id");
        iAgentShareSettingService.closeSharing("ag_test_id");
        BusinessException exception = assertThrows(BusinessException.class,
            () -> iAgentShareSettingService.getAgentIdByShareId(shareId));
        assertThat(exception.getCode()).isEqualTo(
            AgentShareSettingException.AGENT_SHARING_DISABLED.getCode());
    }

    @Test
    void testGetShareSettingNotExists() {
        ShareVO vo = iAgentShareSettingService.getShareSettingByAgentId("ag_test_id");
        assertThat(vo.getShareId()).isNull();
    }


    @Test
    void testGetShareSettingExists() {
        String shareId = iAgentShareSettingService.publishSharing("ag_test_id");
        ShareVO vo = iAgentShareSettingService.getShareSettingByAgentId("ag_test_id");
        assertThat(vo.getShareId()).isEqualTo(shareId);
    }
}
