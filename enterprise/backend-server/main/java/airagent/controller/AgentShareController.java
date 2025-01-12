package com.apitable.enterprise.airagent.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.airagent.model.ShareSimpleVO;
import com.apitable.enterprise.airagent.model.ShareVO;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.enterprise.airagent.service.IAgentShareSettingService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * air-agent share controller.
 */
@RestController
@Tag(name = "AirAgent - Share")
@ApiResource
@Slf4j
public class AgentShareController {
    @Resource
    private IAgentShareSettingService iAgentShareSettingService;
    @Resource
    private IAgentService iAgentService;

    /**
     * publish share.
     *
     * @param agentId agent id
     * @return ShareSimpleVO
     */
    @GetResource(path = "/airagent/ai/{agentId}/share/info", requiredPermission = false)
    @Operation(summary = "Sharing info")
    public ResponseData<ShareVO> shareInfo(@PathVariable("agentId") String agentId) {
        return ResponseData.success(iAgentShareSettingService.getShareSettingByAgentId(agentId));
    }

    /**
     * publish share.
     *
     * @param agentId agent id
     * @return ShareSimpleVO
     */
    @PostResource(path = "/airagent/ai/{agentId}/share/publish", method = RequestMethod.POST, requiredPermission = false)
    @Operation(summary = "Publish Sharing")
    public ResponseData<ShareSimpleVO> publishSharing(@PathVariable("agentId") String agentId) {
        iAgentService.checkAgent(agentId);
        String shareId = iAgentShareSettingService.publishSharing(agentId);
        ShareSimpleVO vo = ShareSimpleVO.builder().shareId(shareId).build();
        return ResponseData.success(vo);
    }


    /**
     * closing share.
     *
     * @param agentId agent id
     */
    @PostResource(path = "/airagent/ai/{agentId}/share/close", method = RequestMethod.PATCH, requiredPermission = false)
    @Operation(summary = "Close sharing")
    public ResponseData<Void> closeSharing(@PathVariable("agentId") String agentId) {
        iAgentService.checkAgent(agentId);
        iAgentShareSettingService.closeSharing(agentId);
        return ResponseData.success();
    }
}
