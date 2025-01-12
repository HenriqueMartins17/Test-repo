package com.apitable.enterprise.ai.server;

import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.server.model.AiInfo;
import com.apitable.enterprise.ai.server.model.ChatCompletion;
import com.apitable.enterprise.ai.server.model.GetSuggestionParams;
import com.apitable.enterprise.ai.server.model.Messages;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.apitable.enterprise.ai.server.model.TrainingInfoList;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;

/**
 * inferences api operation.
 */
public class Inference extends ApiOperation {

    private static final String GET_AI_INFO_TEMPLATE = "/ai/inference/%s";
    private static final String GET_AI_SETTING_TEMPLATE = "/ai/inference/%s/setting";
    private static final String GET_SUGGESTIONS_TEMPLATE = "/ai/inference/%s/suggestions";
    private static final String GET_TRAINING_LIST_TEMPLATE = "/ai/inference/%s/trainings";

    private static final String GET_MESSAGES_TEMPLATE = "/ai/inference/%s/conversations/%s";

    private static final String GET_CONVERSATION_MESSAGES_TEMPLATE =
        "/ai/inference/%s/trainings/%s/conversations/%s";

    private static final String CHAT_COMPLETIONS = "/ai/inference/%s/chat/completions";

    /**
     * get ai info.
     *
     * @param aiId ai unique id
     * @return AiInfo
     * @throws AiServerException ai server call exception
     */
    public static AiInfo getAiInfo(String aiId) throws AiServerException {
        return get(String.format(GET_AI_INFO_TEMPLATE, aiId), AiInfo.class);
    }

    /**
     * get ai setting.
     *
     * @param aiId ai unique id
     * @return PureJson
     * @throws AiServerException ai server call exception
     */
    public static PureJson getAiSetting(String aiId) throws AiServerException {
        return getAiSetting(aiId, null);
    }

    /**
     * get ai setting.
     *
     * @param aiId ai unique id
     * @param type agent type
     * @return PureJson
     * @throws AiServerException ai server call exception
     */
    public static PureJson getAiSetting(String aiId, String type) throws AiServerException {
        String url = String.format(GET_AI_SETTING_TEMPLATE, aiId);
        if (type != null && !type.isEmpty()) {
            url = url + "?type=" + type;
        }
        return get(url, PureJson.class);
    }

    /**
     * get a training list.
     *
     * @param aiId ai unique id
     * @return TrainingInfoList
     * @throws AiServerException ai server call exception
     */
    public static TrainingInfoList getTrainingList(String aiId) throws AiServerException {
        return get(String.format(GET_TRAINING_LIST_TEMPLATE, aiId), TrainingInfoList.class);
    }

    /**
     * get suggestions.
     *
     * @param aiId     ai unique id
     * @param question question
     * @param num      num
     * @return Suggestions
     * @throws AiServerException ai server call exception
     */
    public static Suggestions getSuggestions(String aiId, String question, int num)
        throws AiServerException {
        GetSuggestionParams params = GetSuggestionParams.builder()
            .question(question).n(num)
            .build();
        return post(String.format(GET_SUGGESTIONS_TEMPLATE, aiId), params, Suggestions.class);
    }

    /**
     * get messages.
     *
     * @param aiId           ai unique id
     * @param conversationId conversation id
     * @return Messages
     * @throws AiServerException ai server call exception
     */
    public static Messages getMessages(String aiId, String conversationId, String cursor, int limit)
        throws AiServerException {
        return get(String.format(GET_MESSAGES_TEMPLATE + "?cursor=%s&limit=%d",
            aiId, conversationId, cursor, limit), Messages.class);
    }

    /**
     * get conversation messages.
     *
     * @param aiId           ai unique id
     * @param trainingId     training id
     * @param conversationId conversation id
     * @return Messages
     * @throws AiServerException ai server call exception
     */
    public static Messages getConversationMessages(String aiId, String trainingId,
                                                   String conversationId)
        throws AiServerException {
        return get(
            String.format(GET_CONVERSATION_MESSAGES_TEMPLATE, aiId, trainingId, conversationId),
            Messages.class);
    }

    /**
     * send chat completions.
     *
     * @param aiId           ai unique id
     * @param conversationId conversation id
     * @param content        content
     * @return Flux of String
     * @throws AiServerException ai server call exception
     */
    public static Flux<String> sendChatCompletions(String aiId, String conversationId,
                                                   String content, ChatCallBack callBack)
        throws AiServerException {
        String uri = String.format(CHAT_COMPLETIONS, aiId);
        var chatCompletion = new ChatCompletion(conversationId, content);

        String bodyAsString;

        try {
            bodyAsString = JSON_MAPPER.writeValueAsString(chatCompletion);
        } catch (JsonProcessingException e) {
            throw new AiServerException("failed to write body json to raw string", e);
        }
        return eventStream(uri, bodyAsString, callBack);
    }
}
