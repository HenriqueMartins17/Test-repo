package com.apitable.enterprise.airagent.controller;

import static com.apitable.enterprise.ai.constants.AiConstants.CONVERSATION_ID_HEADER;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.model.Ai;
import com.apitable.enterprise.ai.model.AiInfoVO;
import com.apitable.enterprise.ai.model.AiLoaderConfig;
import com.apitable.enterprise.ai.model.ConversationOrigin;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackCreateParam;
import com.apitable.enterprise.ai.model.FeedbackPagination;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.enterprise.ai.model.FeedbackUpdateParam;
import com.apitable.enterprise.ai.model.FeedbackVO;
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
import com.apitable.enterprise.ai.server.AiServerException;
import com.apitable.enterprise.ai.server.ChatCallBack;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.Message;
import com.apitable.enterprise.ai.server.model.Messages;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiFeedbackService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.ai.service.IAiTrainingService;
import com.apitable.enterprise.airagent.model.AgentCreateRO;
import com.apitable.enterprise.airagent.model.AgentUpdateParams;
import com.apitable.enterprise.airagent.model.AgentVO;
import com.apitable.enterprise.airagent.model.SortedAgents;
import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import com.apitable.enterprise.airagent.model.training.DataSources;
import com.apitable.enterprise.airagent.service.IAgentService;
import com.apitable.enterprise.airagent.service.IAgentShareSettingService;
import com.apitable.enterprise.airagent.service.IDataSourceService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "AirAgent - AI Agent")
@ApiResource
@Slf4j
public class AgentController {

    @Resource
    private IAiService iAiService;

    @Resource
    private IAiTrainingService iAiTrainingService;

    @Resource
    private IDataSourceService iDataSourceService;

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IAiFeedbackService iAiFeedbackService;

    @Resource
    private IAgentService iAgentService;

    @Resource
    private IAgentShareSettingService iAgentShareSettingService;

    /**
     * Get Agent List.
     *
     * @return AiInfoVO
     */
    @GetResource(path = "/airagent/ai", requiredPermission = false)
    @Operation(summary = "Get AI Agent List")
    public ResponseData<List<AgentVO>> list() {
        Long userId = SessionContext.getUserId();
        SortedAgents sortedAgents = iAgentService.getUserAgents(userId);
        List<AgentVO> agentVoList = new ArrayList<>();
        sortedAgents.forEach(agent -> agentVoList.add(AgentVO.of(agent)));
        return ResponseData.success(agentVoList);
    }

    /**
     * Create AI Agent.
     *
     * @param body agent info
     * @return AiInfoVO
     */
    @PostResource(path = "/airagent/ai", requiredPermission = false)
    @Operation(summary = "Create AI Agent")
    public ResponseData<AiInfoVO> create(@RequestBody @Valid AgentCreateRO body) {
        Long userId = SessionContext.getUserId();
        String agentId = iAgentService.create(userId, body);
        Ai ai = iAiService.getAi(agentId,
            AiLoaderConfig.builder().anonymous(false).userId(userId).build());
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Get AI Agent Info.
     *
     * @param agentId agent id
     * @return AiInfoVO
     */
    @GetResource(path = "/airagent/ai/{agentId}", requiredLogin = false)
    @Operation(summary = "Retrieve AI Agent", description = "Retrieve AI Info")
    public ResponseData<AiInfoVO> retrieve(@PathVariable("agentId") String agentId) {
        Long userId = SessionContext.getUserIdWithoutException();
        AiLoaderConfig config = AiLoaderConfig.builder()
            .anonymous(userId == null)
            .userId(userId)
            .build();
        Ai ai = iAiService.getAi(agentId, config);
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Get AI Agent DataSources.
     *
     * @param agentId agent id
     * @return AiInfoVO
     */
    @GetResource(path = "/airagent/ai/{agentId}/datasource", requiredPermission = false)
    @Operation(summary = "Retrieve AI Agent DataSources")
    public ResponseData<DataSources> retrieveDataSources(@PathVariable("agentId") String agentId) {
        DataSources dataSources = iDataSourceService.getDataSources(agentId);
        return ResponseData.success(dataSources);
    }

    /**
     * get info by share id.
     */
    @GetResource(path = "/airagent/ai/share/{shareId}", requiredLogin = false)
    @Operation(summary = "Retrieve AI Agent By shareId")
    public ResponseData<AiInfoVO> retrieveByShareId(@PathVariable("shareId") String shareId) {
        String agentId = iAgentShareSettingService.getAgentIdByShareId(shareId);
        Long userId = SessionContext.getUserIdWithoutException();
        AiLoaderConfig config = AiLoaderConfig.builder()
            .anonymous(userId == null)
            .userId(userId)
            .build();
        Ai ai = iAiService.getAi(agentId, config);
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Retrieve AI Setting.
     *
     * @param agentId ai agent id
     * @return ai setting
     */
    @GetResource(path = "/airagent/ai/{agentId}/setting", requiredPermission = false)
    @Operation(summary = "Retrieve AI Agent Setting")
    public ResponseData<PureJson> retrieveSetting(@PathVariable("agentId") String agentId,
                                                  @RequestParam(name = "type", required = false)
                                                  String type) {
        PureJson settingJson = Inference.getAiSetting(agentId, type);
        return ResponseData.success(settingJson);
    }

    /**
     * Update ai.
     *
     * @param agentId ai id
     * @param payload update param
     * @return AiInfoVO
     */
    @PostResource(path = "/airagent/ai/{agentId}", method = RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update AI Agent", description = "Update AI Info")
    public ResponseData<AiInfoVO> update(@PathVariable("agentId") String agentId,
                                         @RequestBody @Valid AgentUpdateParams payload) {
        Long userId = SessionContext.getUserId();
        iAgentService.update(agentId, payload);
        Ai ai = iAiService.getAi(agentId,
            AiLoaderConfig.builder().anonymous(false).userId(userId).build());
        return ResponseData.success(new AiInfoVO(ai));
    }

    /**
     * Add AI Agent DataSource.
     *
     * @param agentId agent id
     * @return DataSource
     */
    @PostResource(path = "/airagent/ai/{agentId}/datasource", requiredPermission = false)
    @Operation(summary = "Add DataSources")
    public ResponseData<DataSources> addDataSource(
        @PathVariable("agentId") String agentId,
        @RequestBody @Valid DataSourceCreateParams payload) {
        iAgentService.checkAgent(agentId);
        iDataSourceService.addDataSources(agentId, payload);
        DataSources dataSources = iDataSourceService.getDataSources(agentId);
        return ResponseData.success(dataSources);
    }

    /**
     * Retrain DataSources.
     *
     * @param agentId agent id
     * @return void
     */
    @PostResource(path = "/airagent/ai/{agentId}/datasource/{datasourceId}/train", requiredPermission = false)
    @Operation(summary = "Retrain DataSources")
    public ResponseData<Void> retrainDataSource(@PathVariable("agentId") String agentId,
                                                @PathVariable("datasourceId") String datasourceId) {
        iAgentService.checkAgent(agentId);

        return ResponseData.success(null);
    }

    /**
     * Delete DataSources.
     *
     * @param agentId agent id
     * @return void
     */
    @PostResource(path = "/airagent/ai/{agentId}/datasource/{datasourceId}", method = RequestMethod.DELETE, requiredPermission = false)
    @Operation(summary = "Delete DataSources")
    public ResponseData<Void> deleteDataSource(@PathVariable("agentId") String agentId,
                                               @PathVariable("datasourceId") String datasourceId) {
        iAgentService.checkAgent(agentId);
        iDataSourceService.deleteDataSources(datasourceId);
        return ResponseData.success(null);
    }


    /**
     * delete agent.
     *
     * @param agentId agent id.
     */
    @PostResource(path = "/airagent/ai/{agentId}", requiredPermission = false, method = RequestMethod.DELETE)
    @Operation(summary = "Delete AI Agent")
    @Parameters({
        @Parameter(name = "agentId", description = "agent id", required = true, schema = @Schema(type = "string"), in = ParameterIn.PATH, example = "ag_****"),
    })
    public ResponseData<Void> delete(@PathVariable("agentId") String agentId) {
        iAgentService.checkAgent(agentId);
        Long userId = SessionContext.getUserId();
        iAgentService.delete(userId, agentId);
        return ResponseData.success();
    }


    /**
     * train ai agent.
     *
     * @param agentId agent id
     */
    @PostResource(path = "/airagent/ai/{agentId}/train", requiredPermission = false)
    @Operation(summary = "Train AI Agent")
    public ResponseData<Void> train(@PathVariable("agentId") String agentId) {
        iAgentService.checkAgent(agentId);
        iAiService.trainAi(agentId, trainResult -> {
            // do nothing
        });
        return ResponseData.success();
    }

    /**
     * train predict.
     *
     * @param agentId agent id
     * @param payload payload
     * @return TrainingPredictResult
     */
    @PostResource(path = "/airagent/ai/{agentId}/train/predict", requiredPermission = false)
    @Operation(summary = "Train Predict", description = "Train Predict")
    public ResponseData<TrainingPredictResult> trainPredict(
        @PathVariable("agentId") String agentId,
        @RequestBody @Valid TrainingPredictParams payload
    ) {
        iAgentService.checkAgent(agentId);
        TrainingPredictResult result =
            iAiService.getTrainPredict(agentId, payload.getDataSources());
        return ResponseData.success(result);
    }


    /**
     * Retrieve AI suggestions.
     *
     * @param agentId ai agent id
     * @param payload payload
     * @return SuggestionVO
     */
    @PostResource(path = "/airagent/ai/{agentId}/suggestions", requiredLogin = false)
    @Operation(summary = "Get Suggestions", description = "Get Suggestions")
    public ResponseData<SuggestionVO> getSuggestions(@PathVariable("agentId") String agentId,
                                                     @RequestBody @Valid SuggestionParams payload) {
        SessionContext.getUserIdWithoutException();
        Suggestions suggestions = Inference.getSuggestions(agentId, payload.getQuestion(),
            payload.getN());
        return ResponseData.success(new SuggestionVO(agentId, payload.getTrainingId(),
            payload.getConversationId(), suggestions));
    }


    /**
     * Retrieve AI Agent training status.
     *
     * @param agentId ai agent id
     * @return TrainingStatusVO
     */
    @GetResource(path = "/airagent/ai/{agentId}/training/status", requiredLogin = false)
    @Operation(summary = "Retrieve AI Agent Latest Training Status", description = "Retrieve Latest Training Status")
    public ResponseData<TrainingStatusVO> getLastTrainingStatus(
        @PathVariable("agentId") String agentId) {
        SessionContext.getUserIdWithoutException();
        TrainingStatus status = iAiService.getLatestTrainingStatus(agentId);
        return ResponseData.success(new TrainingStatusVO(status));
    }

    /**
     * Retrieve AI  Training List.
     *
     * @param agentId ai id
     * @return ai training list
     */
    @GetResource(path = "/airagent/ai/{agentId}/trainings", requiredPermission = false)
    @Operation(summary = "Retrieve AI Agent Training List", description = "Retrieve AI training list by ai id")
    public ResponseData<List<TrainingInfoVO>> retrieveTrainings(
        @PathVariable("agentId") String agentId) {
        List<TrainingInfoVO> result = iAiTrainingService.getTrainingList(agentId);
        return ResponseData.success(result);
    }

    /**
     * Retrieve Conversation Message.
     *
     * @param agentId        ai agent id
     * @param trainingId     training id
     * @param conversationId conversation id
     * @param cursor         cursor
     * @param limit          limit
     * @return Message
     */
    @GetResource(path = "/airagent/ai/{agentId}/messages", requiredLogin = false)
    @Operation(summary = "Retrieve Conversation Message", description = "Retrieve Conversation Message")
    public ResponseData<Pagination<Message>> getMessagePagination(
        @PathVariable("agentId") String agentId,
        @RequestParam(name = "trainingId", required = false) String trainingId,
        @RequestParam(name = "conversationId", required = false) String conversationId,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {
        SessionContext.getUserIdWithoutException();
        if (!StringUtils.hasLength(conversationId)) {
            return ResponseData.success(Pagination.of(false, new ArrayList<>()));
        }
        if (StringUtils.hasText(trainingId) && StringUtils.hasText(conversationId)) {
            Messages messages =
                Inference.getConversationMessages(agentId, trainingId, conversationId);
            return ResponseData.success(Pagination.of(false, messages));
        }
        Messages messages = Inference.getMessages(agentId, conversationId, cursor, limit);
        return ResponseData.success(Pagination.of(false, messages));
    }

    /**
     * sendMessage.
     *
     * @param agentId  ai agent id
     * @param payload  payload
     * @param response response
     * @return String
     */
    @PostResource(path = "/airagent/ai/{agentId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE, requiredLogin = false)
    @Operation(summary = "Send Message", description = "Send Message")
    public Flux<ServerSentEvent<String>> sendMessage(@PathVariable("agentId") String agentId,
                                                     @RequestBody @Valid SendMessageParam payload,
                                                     HttpServletResponse response) {
        Long userId = SessionContext.getUserIdWithoutException();
        // check if credit nums is over limit, only can send if credit is enough
        iAgentService.checkAgent(agentId);
        iAiService.checkExist(agentId);
        // check conversation id exists, create if anonymous access
        if (payload.getConversationId() == null) {
            // only create conversation id when user sends a message first time
            ConversationOrigin origin =
                userId == null ? ConversationOrigin.ANONYMOUS : ConversationOrigin.INTERNAL;
            int splitLength = 100;
            String title =
                payload.getContent().length() > splitLength ?
                    payload.getContent().substring(0, splitLength)
                    : payload.getContent();
            String conversationId = iAiConversationService.create(agentId, title, origin, userId);
            payload.setConversationId(conversationId);
        } else {
            // check conversation id exists
            iAiConversationService.checkConversation(payload.getConversationId());
        }
        // return conversation id in http header
        response.setHeader(CONVERSATION_ID_HEADER, payload.getConversationId());
        // flux return
        return Inference.sendChatCompletions(agentId, payload.getConversationId(),
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
                        // do nothing
                    }
                }).map(message -> ServerSentEvent.builder(message).build())
            .delayElements(Duration.ofMillis(100));
    }

    /**
     * feedback.
     *
     * @param agentId  ai agent id
     * @param pageNum  pageNum
     * @param pageSize pageSize
     * @param state    state
     * @param search   search
     * @return FeedbackPagination
     */
    @GetResource(path = "/airagent/ai/{agentId}/feedback", requiredPermission = false)
    @Operation(summary = "Retrieve Feedback Pagination", description = "Retrieve Feedback Pagination")
    public ResponseData<FeedbackPagination> getMessagesFeedback(
        @PathVariable("agentId") String agentId,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
        @RequestParam(name = "pageSize", required = false, defaultValue = "20") Integer pageSize,
        @RequestParam(name = "state", required = false) Integer state,
        @RequestParam(name = "search", required = false) String search
    ) {
        SessionContext.getUserId();
        FeedbackQuery query = new FeedbackQuery(pageNum, pageSize, state, search);
        FeedbackPagination pagination = iAiFeedbackService.paginationQuery(agentId, query);
        return ResponseData.success(pagination);
    }

    /**
     * feedback.
     *
     * @param agentId        ai agent id
     * @param conversationId conversation id
     * @return FeedbackVO
     */
    @GetResource(path = "/airagent/ai/{agentId}/conversations/{conversationId}/feedback", requiredPermission = false)
    @Operation(summary = "Retrieve Conversation Feedback", description = "Retrieve Conversation Feedback")
    public ResponseData<FeedbackVO> getConversationFeedback(
        @PathVariable("agentId") String agentId,
        @PathVariable(name = "conversationId") String conversationId
    ) {
        SessionContext.getUserId();
        List<Feedback> feedbacks = iAiFeedbackService.getAiFeedbackByConversationId(agentId,
            conversationId);
        return ResponseData.success(new FeedbackVO(feedbacks));
    }

    /**
     * feedback.
     *
     * @param agentId aiId
     * @param payload payload
     * @return Feedback
     */
    @PostResource(path = "/airagent/ai/{agentId}/feedback", requiredLogin = false)
    @Operation(summary = "Create Feedback", description = "Create Feedback")
    public ResponseData<Feedback> createFeedback(
        @PathVariable("agentId") String agentId, @RequestBody @Valid FeedbackCreateParam payload
    ) {
        Long userId = SessionContext.getUserIdWithoutException();
        iAgentService.checkAgent(agentId);
        AiEntity aiEntity = iAiService.getByAiId(agentId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        Feedback feedback = iAiFeedbackService.create(
            aiEntity.getSpaceId(),
            agentId,
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
     * feedback.
     *
     * @param agentId    aiId
     * @param feedbackId feedbackId
     * @param payload    payload
     */
    @PostResource(path = "/airagent/ai/{aiId}/feedback/{feedbackId}", method = RequestMethod.PUT, requiredPermission = false)
    @Operation(summary = "Update Feedback", description = "Update Feedback")
    public ResponseData<Void> updateFeedback(
        @PathVariable("aiId") String agentId,
        @PathVariable("feedbackId") Long feedbackId,
        @RequestBody @Valid FeedbackUpdateParam payload
    ) {
        SessionContext.getUserIdWithoutException();
        iAgentService.checkAgent(agentId);
        boolean affectRows = iAiFeedbackService.updateState(feedbackId, payload.getState());
        if (!affectRows) {
            throw new BusinessException(AiException.UPDATE_FEEDBACK_STATE_FAIL);
        }
        return ResponseData.success();
    }
}
