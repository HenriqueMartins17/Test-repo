package com.apitable.enterprise.airagent.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.airagent.model.UserProfile;
import com.apitable.enterprise.airagent.service.IAgentUserService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * air agent user controller.
 */
@RestController
@Tag(name = "AirAgent - User")
@ApiResource
@Slf4j
public class AgentUserController {

    @Resource
    private IAgentUserService iAgentUserService;

    /**
     * Get User Profile.
     *
     * @return user profile
     */
    @GetResource(path = "/airagent/user/profile", requiredPermission = false)
    @Operation(summary = "Get User Profile")
    public ResponseData<UserProfile> getUserProfile() {
        Long userId = SessionContext.getUserId();
        UserProfile userProfile = iAgentUserService.getUserProfile(userId);
        return ResponseData.success(userProfile);
    }
}
