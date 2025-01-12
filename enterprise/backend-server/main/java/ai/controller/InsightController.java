package com.apitable.enterprise.ai.controller;

import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.model.Conversation;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiFeedbackService;
import com.apitable.enterprise.ai.service.IAiPermissionService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * insight controller.
 */
@RestController
@Tag(name = "AI - Insight", description = "Insight API are used to manage ai agent insight")
@ApiResource
public class InsightController {

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IAiFeedbackService iAiFeedbackService;

    @Resource
    private IAiPermissionService iAiPermissionService;

    @GetResource(path = "/insight/conversations", requiredPermission = false)
    @Operation(summary = "List conversations", description = "List all conversations of ai agent")
    public ResponseData<PageInfo<Conversation>> listConversation(
        @RequestParam("aiId") String aiId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        PageRequest pageable = PageRequest.of(pageNum, pageSize);
        PageInfo<Conversation> result = iAiConversationService.pagination(aiId, pageable);
        return ResponseData.success(result);
    }

    /**
     * Retrieve Feedback Pagination.
     *
     * @param aiId     ai id
     * @param pageNum  page num
     * @param pageSize page size
     * @param state    state
     * @param search   search
     * @return feedback pagination
     */
    @GetResource(path = "/insight/feedbacks", requiredPermission = false)
    @Operation(summary = "List Feedbacks", description = "List all feedbacks of ai agent")
    public ResponseData<PageInfo<Feedback>> getMessagesFeedback(
        @RequestParam("aiId") String aiId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @RequestParam(name = "state", required = false) Integer state,
        @RequestParam(name = "search", required = false) String search
    ) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        FeedbackQuery query = new FeedbackQuery(pageNum, pageSize, state, search);
        PageInfo<Feedback> pagination = iAiFeedbackService.pagination(aiId, query);
        return ResponseData.success(pagination);
    }
}
