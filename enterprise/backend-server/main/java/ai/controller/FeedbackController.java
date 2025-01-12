package com.apitable.enterprise.ai.controller;

import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackCreateParams;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.enterprise.ai.model.FeedbackUpdateParam;
import com.apitable.enterprise.ai.service.IAiFeedbackService;
import com.apitable.enterprise.ai.service.IAiPermissionService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * feedback controller.
 */
@RestController
@Tag(name = "AI - Feedback", description = "Feedback API are used to manage feedback")
@ApiResource
public class FeedbackController {

    @Resource
    private IAiService iAiService;

    @Resource
    private IAiFeedbackService iAiFeedbackService;

    @Resource
    private IAiPermissionService iAiPermissionService;

    @GetResource(path = "/feedbacks", requiredLogin = false)
    @Operation(summary = "List Feedbacks", description = "List Feedback of specific conversation")
    public ResponseData<PageInfo<Feedback>> getConversationFeedback(
        @RequestParam("aiId") String aiId,
        @RequestParam("conversationId") String conversationId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        FeedbackQuery query = new FeedbackQuery(pageNum, pageSize);
        PageInfo<Feedback> pagination = iAiFeedbackService.pagination(aiId, conversationId, query);
        return ResponseData.success(pagination);
    }

    @PostResource(path = "/feedbacks", requiredLogin = false)
    @Operation(summary = "Create Feedback", description = "Create Feedback of specific conversation message")
    public ResponseData<Feedback> createFeedback(@RequestBody @Valid FeedbackCreateParams payload) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(payload.getAiId(), userId);
        AiEntity aiEntity = iAiService.getByAiId(payload.getAiId());
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        Feedback feedback = iAiFeedbackService.create(
            aiEntity.getSpaceId(),
            payload.getAiId(),
            payload.getTrainingId(),
            payload.getConversationId(),
            payload.getMessageIndex(),
            payload.getLike(),
            payload.getComment(),
            userId
        );
        return ResponseData.success(feedback);
    }

    @PostResource(path = "/feedbacks/{feedbackId}", method = RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update Feedback", description = "Update Feedback")
    public ResponseData<Void> updateFeedback(
        @PathVariable("feedbackId") Long feedbackId,
        @RequestBody @Valid FeedbackUpdateParam payload
    ) {
        Long userId = SessionContext.getUserId();
        String aiId = iAiFeedbackService.getAiIdById(feedbackId);
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        boolean affectRows = iAiFeedbackService.updateState(feedbackId, payload.getState());
        if (!affectRows) {
            throw new BusinessException(AiException.UPDATE_FEEDBACK_STATE_FAIL);
        }
        return ResponseData.success();
    }
}
