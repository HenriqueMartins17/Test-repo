package com.apitable.enterprise.ai.controller;

import static com.apitable.enterprise.ai.exception.CopilotException.COPILOT_CONVERSATION_NOT_FOUND;

import com.apitable.core.exception.BusinessException;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.ai.autoconfigure.AiServerProperties;
import com.apitable.enterprise.ai.model.ConversationCreatePrams;
import com.apitable.enterprise.ai.model.Copilot;
import com.apitable.enterprise.ai.model.CopilotChatRequestParam;
import com.apitable.enterprise.ai.model.CopilotConversation;
import com.apitable.enterprise.ai.model.CopilotSuggestionParams;
import com.apitable.enterprise.ai.model.LatestConversation;
import com.apitable.enterprise.ai.model.Pagination;
import com.apitable.enterprise.ai.server.AiServerException;
import com.apitable.enterprise.ai.server.ChatCallBack;
import com.apitable.enterprise.ai.server.Copilots;
import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.apitable.enterprise.ai.server.model.CopilotChatCompletion;
import com.apitable.enterprise.ai.server.model.CopilotChatMessage;
import com.apitable.enterprise.ai.server.model.CopilotChatMessages;
import com.apitable.enterprise.ai.server.model.CopilotChatMetadata;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.apitable.enterprise.ai.service.ICopilotConversationService;
import com.apitable.organization.service.IMemberService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import reactor.core.publisher.Flux;

/**
 * copilot controller.
 */
@RestController
@Tag(name = "AI - Copilot", description = "Copilot API")
@ApiResource
@Slf4j
public class CopilotController {

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ICopilotConversationService iCopilotConversationService;

    @Resource
    private AiServerProperties aiServerProperties;

    @GetResource(path = "/ai/copilot", requiredPermission = false)
    @Operation(summary = "Retrieve Copilot", description = "Retrieve Copilot contains latest conversation.")
    public ResponseData<Copilot> create(@RequestParam(name = "spaceId") String spaceId) {
        Long userId = SessionContext.getUserId();
        // check user is in space
        iMemberService.checkUserIfInSpace(userId, spaceId);
        LatestConversation latestConversation =
            iCopilotConversationService.retrieveLatestConversationId(userId, spaceId);
        CopilotConversation conversation =
            iCopilotConversationService.retrieve(latestConversation.getLatestConversationId());
        Copilot copilot = new Copilot();
        copilot.setFirstTimeUsed(latestConversation.isFirstTimeUsed());
        copilot.setLatestConversation(conversation);
        return ResponseData.success(copilot);
    }

    @PostResource(path = "/ai/copilot/suggestions", requiredPermission = false)
    @Operation(summary = "Retrieve Suggestions", description = "Retrieve Suggestions with copilot assistant type.")
    public ResponseData<Suggestions> retrieveSuggestion(
        @RequestBody @Valid CopilotSuggestionParams payload
    ) {
        Suggestions suggestions = Copilots.getSuggestions(payload.getType());
        return ResponseData.success(suggestions);
    }

    @PostResource(path = "/ai/copilot/conversations", requiredPermission = false)
    @Operation(summary = "Create Conversations", description = "Update Conversations.")
    public ResponseData<CopilotConversation> createConversations(
        @RequestBody @Valid ConversationCreatePrams params
    ) {
        String conversationId =
            iCopilotConversationService.create(params.getSpaceId(), params.getType().name(),
                params.getType());
        CopilotConversation conversation = iCopilotConversationService.retrieve(conversationId);
        return ResponseData.success(conversation);
    }

    @GetResource(path = "/ai/copilot/conversations/{conversationId}", requiredPermission = false)
    @Operation(summary = "Retrieve Conversations", description = "Update Conversations.")
    public ResponseData<CopilotConversation> retrieveConversations(
        @PathVariable("conversationId") String conversationId
    ) {
        CopilotConversation conversation = iCopilotConversationService.retrieve(conversationId);
        return ResponseData.success(conversation);
    }

    @GetResource(path = "/ai/copilot/conversations/{conversationId}/messages", requiredPermission = false)
    @Operation(summary = "Retrieve Conversation Message", description = "Returns a list of messages for a given conversation")
    public ResponseData<Pagination<CopilotChatMessage>> getMessagePagination(
        @PathVariable("conversationId") String conversationId,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {
        if (!StringUtils.hasLength(conversationId)) {
            return ResponseData.success(Pagination.of(false, new ArrayList<>()));
        }
        CopilotChatMessages messages = Copilots.getMessages(conversationId, cursor, limit);
        return ResponseData.success(Pagination.of(false, messages));
    }

    @PostResource(path = "/ai/copilot/conversations/{conversationId}/cancel", requiredPermission = false)
    @Operation(summary = "Cancel a Conversation Response", description = "Cancels a conversation that is in progress.")
    public ResponseData<Void> cancelConversationResponse(
        @PathVariable("conversationId") String conversationId
    ) {
        Copilots.cancelConversation(conversationId);
        return ResponseData.success();
    }

    @PostResource(path = "/ai/copilot/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE, requiredPermission = false)
    @Operation(summary = "Chat Completions", description = "Creates a model response for the given chat conversation.")
    public Flux<ServerSentEvent<String>> sendMessage(
        @RequestBody @Valid CopilotChatRequestParam payload) {
        // check conversation id exists, create if anonymous access
        Long userId = SessionContext.getUserId();
        // check conversation id exists
        var conversationEntity =
            iCopilotConversationService.getUserConversation(payload.getConversationId(), userId);
        if (conversationEntity == null) {
            throw new BusinessException(COPILOT_CONVERSATION_NOT_FOUND);
        }
        CopilotAssistantType copilotAssistantType =
            CopilotAssistantType.of(conversationEntity.getType());
        CopilotChatCompletion chatCompletion = new CopilotChatCompletion(
            copilotAssistantType, payload.getConversationId(), payload.getContent(),
            new CopilotChatMetadata(payload.getDatasheetId(), payload.getViewId())
        );
        // flux return
        return Copilots.chatCompletions(chatCompletion, new ChatCallBack() {
                @Override
                public void onCancel() {
                    log.info("client cancel the chat completions manually");
                    // manually cancel conversation
                    Copilots.cancelConversation(payload.getConversationId());
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

    @GetResource(path = "/ai/copilot/files/{fileId}/content", requiredPermission = false)
    @Operation(summary = "Retrieve Files content", description = "Retrieve files content of a specific ai agent")
    public void retrieveFileContent(
        @PathVariable("fileId") String fileId,
        HttpServletResponse response
    ) throws IOException {
        byte[] bodyAsBytes;
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(aiServerProperties.getOpenai().getApiKey());
            RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(aiServerProperties.getOpenai().getBaseUrl())
                .defaultHeaders(header -> header.addAll(headers))
                .build();

            bodyAsBytes = restClient
                .get()
                .uri("/files/" + fileId + "/content")
                .retrieve()
                .body(byte[].class);
        } catch (IOException e) {
            throw new ServerErrorException("Error to retrieve file content", e);
        }

        if (bodyAsBytes == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "file content is empty");
        }

        // write response data
        ServletOutputStream out = response.getOutputStream();
        out.write(bodyAsBytes);
        out.flush();
    }
}
