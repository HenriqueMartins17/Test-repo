package com.apitable.enterprise.ai.controller;

import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.model.Conversation;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiPermissionService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * conversation controller.
 */
@RestController
@Tag(name = "AI - Conversation", description = "Conversation API are used to manage conversation")
@ApiResource
public class ConversationController {

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IAiPermissionService iAiPermissionService;

    @GetResource(path = "/ai/{aiId}/conversations", requiredPermission = false)
    @Operation(summary = "List conversations", description = "List a specific user chat conversations of ai agent")
    public ResponseData<PageInfo<Conversation>> list(
        @PathVariable("aiId") String aiId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.anonymousValid(aiId, userId);
        PageRequest pageable = PageRequest.of(pageNum, pageSize);
        PageInfo<Conversation> result =
            iAiConversationService.userPagination(userId, aiId, pageable);
        return ResponseData.success(result);
    }

    @GetResource(path = "/conversations/{conversationId}", requiredPermission = false)
    @Operation(summary = "Retrieve conversation", description = "Retrieve information about a specific conversation")
    public ResponseData<Conversation> retrieve(
        @PathVariable("conversationId") String conversationId) {
        Long userId = SessionContext.getUserId();
        String aiId = iAiConversationService.getAiIdByConversationId(conversationId);
        iAiPermissionService.anonymousValid(aiId, userId);
        Conversation conversation = iAiConversationService.retrieve(conversationId);
        return ResponseData.success(conversation);
    }
}
