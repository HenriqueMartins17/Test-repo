package com.apitable.enterprise.ai.controller;

import static com.apitable.enterprise.ai.constants.AiConstants.CONVERSATION_ID_HEADER;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_EXCHANGE_MAIN;
import static com.apitable.enterprise.ai.constants.AiConstants.TRAINING_TRANSACTION_ROUTE_KEY;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.entity.AiConversationEntity;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.model.Ai;
import com.apitable.enterprise.ai.model.AiInfoVO;
import com.apitable.enterprise.ai.model.AiLoaderConfig;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.AiUpdateParams;
import com.apitable.enterprise.ai.model.ConversationOrigin;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackCreateParam;
import com.apitable.enterprise.ai.model.FeedbackPagination;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.enterprise.ai.model.FeedbackUpdateParam;
import com.apitable.enterprise.ai.model.FeedbackVO;
import com.apitable.enterprise.ai.model.MessageCreditLimit;
import com.apitable.enterprise.ai.model.MessageCreditUsageVO;
import com.apitable.enterprise.ai.model.Pagination;
import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.model.SendMessageParam;
import com.apitable.enterprise.ai.model.SuggestionParams;
import com.apitable.enterprise.ai.model.SuggestionVO;
import com.apitable.enterprise.ai.model.TrainingInfoVO;
import com.apitable.enterprise.ai.model.TrainingPredictParams;
import com.apitable.enterprise.ai.model.TrainingPredictResult;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.model.TrainingStatusVO;
import com.apitable.enterprise.ai.queue.TrainingMessage;
import com.apitable.enterprise.ai.server.AiServerException;
import com.apitable.enterprise.ai.server.ChatCallBack;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.Message;
import com.apitable.enterprise.ai.server.model.Messages;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiCreditService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.enterprise.ai.service.IAiFeedbackService;
import com.apitable.enterprise.ai.service.IAiPermissionService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.ai.service.IAiTrainingService;
import com.apitable.interfaces.ai.model.AiType;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.workspace.service.INodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI Controller.
 */
@RestController
@Tag(name = "AI - ChatBot")
@ApiResource
@Slf4j
public class AiController {

    @Resource
    private IAiService iAiService;

    @Resource
    private IAiTrainingService iAiTrainingService;

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IAiFeedbackService iAiFeedbackService;

    @Resource
    private IAiCreditService iAiCreditService;

    @Resource
    private IAiCreditTransactionService iAiCreditTransactionService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IAiPermissionService iAiPermissionService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private void wrapperCredit(Ai ai) {
        String spaceId = iNodeService.getSpaceIdByNodeId(ai.getId());
        String model = MapUtil.getStr(ai.getSetting(), "model");
        AiModel aiModel = AiModel.of(model);
        MessageCreditLimit messageCreditLimit =
            iAiCreditService.getMessageCreditLimit(spaceId, aiModel);
        ai.setMessageCreditLimit(messageCreditLimit);
    }

    private void wrapperFormName(Ai ai) {
        Map<String, Object> setting = ai.getSetting();
        if (setting != null && setting.containsKey("formId")) {
            String formId = MapUtil.getStr(setting, "formId");
            if (StrUtil.isNotBlank(formId)) {
                String formName = iNodeService.getNodeNameByNodeId(formId);
                setting.put("formName", formName);
                ai.setSetting(setting);
            }
        }
    }

    /**
     * Retrieve AI Info.
     *
     * @param aiId ai id
     * @return ai info
     */
    @GetResource(path = "/ai/{aiId}", requiredLogin = false)
    @Operation(summary = "Retrieve AI Info", description = "Retrieve AI Info by ai id")
    public ResponseData<AiInfoVO> retrieve(@PathVariable("aiId") String aiId) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        AiLoaderConfig config = AiLoaderConfig.builder()
            .anonymous(userId == null)
            .userId(userId)
            .build();
        Ai ai = iAiService.getAi(aiId, config);
        wrapperFormName(ai);
        wrapperCredit(ai);
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Retrieve AI Setting.
     *
     * @param aiId ai id
     * @return ai setting
     */
    @GetResource(path = "/ai/{aiId}/setting", requiredPermission = false)
    @Operation(summary = "Retrieve AI Setting", description = "Retrieve AI Setting by ai id")
    public ResponseData<PureJson> retrieveSetting(@PathVariable("aiId") String aiId,
                                                  @RequestParam(name = "type", required = false)
                                                  String type) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        PureJson settingJson = Inference.getAiSetting(aiId, type);
        return ResponseData.success(settingJson);
    }

    /**
     * Retrieve AI Training List.
     *
     * @param aiId ai id
     * @return ai training list
     */
    @GetResource(path = "/ai/{aiId}/trainings", requiredPermission = false)
    @Operation(summary = "Retrieve AI Training List", description = "Retrieve AI training list by ai id")
    public ResponseData<List<TrainingInfoVO>> retrieveTrainings(@PathVariable("aiId") String aiId) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        List<TrainingInfoVO> result = iAiTrainingService.getTrainingList(aiId);
        return ResponseData.success(result);
    }

    /**
     * Retrieve AI Training Info.
     *
     * @param aiId    ai id
     * @param payload request body
     * @return ai info
     */
    @PostResource(path = "/ai/{aiId}", method = RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update AI Info", description = "Update AI Info")
    public ResponseData<AiInfoVO> update(@PathVariable("aiId") String aiId,
                                         @RequestBody @Valid AiUpdateParams payload) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        iAiService.updateAi(aiId, payload);
        Ai ai = iAiService.getAi(aiId,
            AiLoaderConfig.builder().anonymous(false).userId(userId).build());
        wrapperFormName(ai);
        wrapperCredit(ai);
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Train AI.
     *
     * @param aiId ai id
     * @return ai training info
     */
    @PostResource(path = "/ai/{aiId}/train", requiredPermission = false)
    @Operation(summary = "Train", description = "Train")
    public ResponseData<Void> train(@PathVariable("aiId") String aiId) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        iAiService.train(aiId, trainResult -> {
            AiEntity aiEntity = iAiService.getByAiId(aiId);
            // only qa type need to send message to mq
            if (aiEntity != null && aiEntity.getType().equals(AiType.QA.getValue())) {
                // send message to mq
                TrainingMessage
                    message = new TrainingMessage(aiId, trainResult.getNewTrainingId(), userId);
                // use TrainingQueueConsumer listener to handle training status trace
                rabbitTemplate.convertAndSend(TRAINING_EXCHANGE_MAIN,
                    TRAINING_TRANSACTION_ROUTE_KEY,
                    message);
            }
        });
        return ResponseData.success();
    }

    /**
     * Train Predict.
     *
     * @param aiId    ai id
     * @param payload request body
     * @return ai training info
     */
    @PostResource(path = "/ai/{aiId}/train/predict", requiredPermission = false)
    @Operation(summary = "Train Predict", description = "Train Predict")
    public ResponseData<TrainingPredictResult> trainPredict(@PathVariable("aiId") String aiId,
                                                            @RequestBody
                                                            @Valid TrainingPredictParams payload) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        TrainingPredictResult result = iAiService.getTrainPredict(aiId, payload.getDataSources());
        return ResponseData.success(result);
    }

    /**
     * Retrieve Suggestions.
     *
     * @param aiId    ai id
     * @param payload request body
     * @return ai training info
     */
    @PostResource(path = "/ai/{aiId}/suggestions", requiredLogin = false)
    @Operation(summary = "Get Suggestions", description = "Get Suggestions")
    public ResponseData<SuggestionVO> getSuggestions(@PathVariable("aiId") String aiId,
                                                     @RequestBody @Valid SuggestionParams payload) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        Suggestions suggestions = Inference.getSuggestions(aiId, payload.getQuestion(),
            payload.getN());
        return ResponseData.success(new SuggestionVO(aiId, payload.getTrainingId(),
            payload.getConversationId(), suggestions));
    }

    /**
     * Retrieve Latest Training Status.
     *
     * @param aiId ai id
     * @return training status
     */
    @GetResource(path = "/ai/{aiId}/training/status", requiredLogin = false)
    @Operation(summary = "Retrieve Latest Training Status", description = "Retrieve Latest Training Status")
    public ResponseData<TrainingStatusVO> getLastTrainingStatus(@PathVariable("aiId") String aiId) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        TrainingStatus status = iAiService.getLatestTrainingStatus(aiId);
        return ResponseData.success(new TrainingStatusVO(status));
    }

    /**
     * Retrieve Credit Usage.
     *
     * @param aiId ai id
     * @return credit usage
     */
    @GetResource(path = "/ai/{aiId}/credit/usage", requiredLogin = false)
    @Operation(summary = "Retrieve Credit Usage", description = "Retrieve Latest Training Status")
    public ResponseData<MessageCreditUsageVO> getCreditUsage(@PathVariable("aiId") String aiId) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        AiModel aiModel = AiModel.of(aiEntity.getModel());
        MessageCreditLimit creditLimit =
            iAiCreditService.getMessageCreditLimit(aiEntity.getSpaceId(), aiModel);
        return ResponseData.success(new MessageCreditUsageVO(creditLimit));
    }

    /**
     * Retrieve Conversation Message Pagination.
     *
     * @param aiId           ai id
     * @param trainingId     training id
     * @param conversationId conversation id
     * @param cursor         cursor
     * @param limit          limit
     * @return conversation message pagination
     */
    @GetResource(path = "/ai/{aiId}/messages", requiredLogin = false)
    @Operation(summary = "Retrieve Conversation Message", description = "Retrieve Conversation Message")
    public ResponseData<Pagination<Message>> getMessagePagination(
        @PathVariable("aiId") String aiId,
        @RequestParam(name = "trainingId", required = false) String trainingId,
        @RequestParam(name = "conversationId", required = false) String conversationId,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        if (!StringUtils.hasLength(conversationId)) {
            return ResponseData.success(Pagination.of(false, new ArrayList<>()));
        }
        if (StringUtils.hasText(trainingId) && StringUtils.hasText(conversationId)) {
            Messages messages = Inference.getConversationMessages(aiId, trainingId, conversationId);
            return ResponseData.success(Pagination.of(false, messages));
        }
        AiConversationEntity conversationEntity =
            iAiConversationService.getByConversationId(conversationId);
        if (conversationEntity == null) {
            throw new BusinessException(AiException.CONVERSATION_NOT_FOUND);
        }
        if (StringUtils.hasText(conversationEntity.getTrainingId())) {
            Messages messages =
                Inference.getConversationMessages(aiId, conversationEntity.getTrainingId(),
                    conversationId);
            return ResponseData.success(Pagination.of(false, messages));
        }
        Messages messages = Inference.getMessages(aiId, conversationId, cursor, limit);
        return ResponseData.success(Pagination.of(false, messages));
    }

    /**
     * Send Message.
     *
     * @param aiId     ai id
     * @param payload  request body
     * @param response http response
     * @return message flux
     */
    @PostResource(path = "/ai/{aiId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE, requiredLogin = false)
    @Operation(summary = "Send Message", description = "Send Message")
    public Flux<ServerSentEvent<String>> sendMessage(@PathVariable("aiId") String aiId,
                                                     @RequestBody @Valid SendMessageParam payload,
                                                     HttpServletResponse response) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        // check if credit nums is over limit, only can send if credit is enough
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        iAiCreditService.checkChatCredit(aiEntity.getSpaceId(), AiModel.of(aiEntity.getModel()));
        // check conversation id exists, create if anonymous access
        if (!StringUtils.hasText(payload.getConversationId())) {
            // only create conversation id when user sends a message first time
            ConversationOrigin origin =
                userId == null ? ConversationOrigin.ANONYMOUS : ConversationOrigin.INTERNAL;
            // simple conversation title
            int splitLength = 100;
            String title =
                payload.getContent().length() > splitLength ?
                    payload.getContent().substring(0, splitLength)
                    : payload.getContent();
            String conversationId = iAiConversationService.create(aiId, title, origin, userId);
            payload.setConversationId(conversationId);
        } else {
            // check conversation id exists
            iAiConversationService.checkConversation(payload.getConversationId());
        }
        // return conversation id in http header
        response.setHeader(CONVERSATION_ID_HEADER, payload.getConversationId());
        // flux return
        return Inference.sendChatCompletions(aiId, payload.getConversationId(),
                payload.getContent(), new ChatCallBack() {
                    @Override
                    public void onCancel() {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        log.error("Error to call chat completions", throwable);
                        throw new AiServerException("Error to call chat completions", throwable);
                    }

                    @Override
                    public void onComplete() {
                        AiModel aiModel = AiModel.of(aiEntity.getModel());
                        iAiCreditTransactionService.saveTransaction(aiEntity.getSpaceId(), aiId,
                            aiModel,
                            payload.getConversationId(), userId);
                    }
                }).map(message -> ServerSentEvent.builder(message).build())
            .delayElements(Duration.ofMillis(100));
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
    @GetResource(path = "/ai/{aiId}/feedback", requiredPermission = false)
    @Operation(summary = "Retrieve Feedback Pagination", description = "Retrieve Feedback Pagination", deprecated = true)
    public ResponseData<FeedbackPagination> getMessagesFeedback(
        @PathVariable("aiId") String aiId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @RequestParam(name = "state", required = false) Integer state,
        @RequestParam(name = "search", required = false) String search
    ) {
        Long userId = SessionContext.getUserId();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        FeedbackQuery query = new FeedbackQuery(pageNum, pageSize, state, search);
        FeedbackPagination pagination = iAiFeedbackService.paginationQuery(aiId, query);
        return ResponseData.success(pagination);
    }

    /**
     * Retrieve Conversation Feedback.
     *
     * @param aiId           ai id
     * @param conversationId conversation id
     * @return feedback vo
     */
    @GetResource(path = "/ai/{aiId}/conversations/{conversationId}/feedback", requiredLogin = false)
    @Operation(summary = "Retrieve Conversation Feedback", description = "Retrieve Conversation Feedback", deprecated = true)
    public ResponseData<FeedbackVO> getConversationFeedback(
        @PathVariable("aiId") String aiId,
        @PathVariable("conversationId") String conversationId
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        List<Feedback> feedbacks = iAiFeedbackService.getAiFeedbackByConversationId(aiId,
            conversationId);
        return ResponseData.success(new FeedbackVO(feedbacks));
    }

    /**
     * Create Feedback.
     *
     * @param aiId    ai id
     * @param payload request body
     * @return feedback
     */
    @PostResource(path = "/ai/{aiId}/feedback", requiredLogin = false)
    @Operation(summary = "Create Feedback", description = "Create Feedback", deprecated = true)
    public ResponseData<Feedback> createFeedback(
        @PathVariable("aiId") String aiId, @RequestBody @Valid FeedbackCreateParam payload
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.anonymousValid(aiId, userId);
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        Feedback feedback = iAiFeedbackService.create(
            aiEntity.getSpaceId(),
            aiId,
            payload.getTrainingId(),
            payload.getConversationId(),
            payload.getMessageIndex(),
            payload.getLike(),
            payload.getComment(),
            userId
        );
        return ResponseData.success(feedback);
    }

    /**
     * Update Feedback.
     *
     * @param aiId       ai id
     * @param feedbackId feedback id
     * @param payload    request body
     */
    @PostResource(path = "/ai/{aiId}/feedback/{feedbackId}", method = RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update Feedback", description = "Update Feedback", deprecated = true)
    public ResponseData<Void> updateFeedback(
        @PathVariable("aiId") String aiId,
        @PathVariable("feedbackId") Long feedbackId,
        @RequestBody @Valid FeedbackUpdateParam payload
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAiPermissionService.validPermission(aiId, userId, NodePermission.MANAGE_NODE);
        boolean affectRows = iAiFeedbackService.updateState(feedbackId, payload.getState());
        if (!affectRows) {
            throw new BusinessException(AiException.UPDATE_FEEDBACK_STATE_FAIL);
        }
        return ResponseData.success();
    }
}
