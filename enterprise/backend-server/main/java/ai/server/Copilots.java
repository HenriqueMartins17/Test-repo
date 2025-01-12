package com.apitable.enterprise.ai.server;

import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.apitable.enterprise.ai.server.model.CopilotChatCompletion;
import com.apitable.enterprise.ai.server.model.CopilotChatMessages;
import com.apitable.enterprise.ai.server.model.CopilotGetSuggestionParams;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * copilot api operation.
 */
public class Copilots extends ApiOperation {

    private static final String CHAT_COMPLETIONS = "/ai/copilot/chat/completions";
    private static final String GET_CONVERSATIONS = "/ai/copilot/conversations/%s";
    private static final String CANCEL_CONVERSATIONS = "/ai/copilot/conversations/%s/runs/cancel";
    private static final String GET_SETTING = "/ai/copilot/setting";
    private static final String GET_SUGGESTIONS = "/ai/copilot/suggestions";

    /**
     * get copilot prologue.
     *
     * @return prologue
     * @throws AiServerException ai server call exception
     */
    public static String getPrologue(CopilotAssistantType type)
        throws AiServerException {
        String uri = String.format(GET_SETTING + "?type=%s", type.getValue());
        PureJson setting = get(uri, PureJson.class);
        if (setting == null) {
            return null;
        }
        Map<String, Object> data = setting.extractData();
        return data != null && !data.isEmpty() ? data.get("prologue").toString() : null;
    }

    /**
     * get suggestions.
     *
     * @param type copilot type
     * @return suggestion list
     * @throws AiServerException ai server call exception
     */
    public static Suggestions getSuggestions(CopilotAssistantType type)
        throws AiServerException {
        CopilotGetSuggestionParams params = CopilotGetSuggestionParams.builder()
            .assistantType(type)
            .build();
        return post(GET_SUGGESTIONS, params, Suggestions.class);
    }

    /**
     * get messages.
     *
     * @param conversationId conversation id
     * @param cursor         cursor
     * @param limit          limit
     * @return Messages
     * @throws AiServerException ai server call exception
     */
    public static CopilotChatMessages getMessages(String conversationId, String cursor, int limit)
        throws AiServerException {
        String uri =
            String.format(GET_CONVERSATIONS + "?cursor=%s&limit=%d", conversationId, cursor, limit);
        return get(uri, CopilotChatMessages.class);
    }

    /**
     * cancel conversation.
     *
     * @param conversationId conversation id
     */
    public static void cancelConversation(String conversationId) {
        String uri = String.format(CANCEL_CONVERSATIONS, conversationId);
        post(uri, Void.class);
    }

    /**
     * chat completions.
     *
     * @param chatCompletion chat completion request body
     * @param callBack       call back
     * @return Flux
     * @throws AiServerException ai server call exception
     */
    public static Flux<String> chatCompletions(
        CopilotChatCompletion chatCompletion,
        ChatCallBack callBack) throws AiServerException {
        String bodyAsString;
        try {
            bodyAsString = JSON_MAPPER.writeValueAsString(chatCompletion);
        } catch (JsonProcessingException e) {
            throw new AiServerException("failed to write body json to raw string", e);
        }
        return eventStream(CHAT_COMPLETIONS, bodyAsString, callBack);
    }
}
