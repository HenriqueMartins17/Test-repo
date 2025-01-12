package com.apitable.enterprise.ai.server;

import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.server.model.AiInfo;
import com.apitable.enterprise.ai.server.model.Messages;
import com.apitable.enterprise.ai.server.model.PostTrainResult;
import com.apitable.enterprise.ai.server.model.Suggestions;
import com.apitable.enterprise.ai.server.model.TrainingInfo;
import com.apitable.enterprise.ai.server.model.TrainingInfoList;
import lombok.Getter;
import reactor.core.publisher.Flux;

/**
 * ai api.
 */
@Getter
public class AiAPI {

    private final String aiId;
    private final String type;

    public AiAPI(String aiId) {
        this(aiId, null);
    }

    public AiAPI(String aiId, String type) {
        this.aiId = aiId;
        this.type = type;
    }

    public static AiAPI create(String aiId) {
        return new AiAPI(aiId);
    }

    public static AiAPI create(String aiId, String type) {
        return new AiAPI(aiId, type);
    }

    public AiInfo getAiInfo() {
        return Inference.getAiInfo(aiId);
    }

    public PureJson getSetting() {
        return Inference.getAiSetting(aiId);
    }

    public PureJson getSetting(String type) {
        return Inference.getAiSetting(aiId, type);
    }

    public Suggestions getSuggestions(String question, int num) {
        return Inference.getSuggestions(aiId, question, num);
    }

    public Messages getMessages(String conversationId, String cursor, int limit) {
        return Inference.getMessages(aiId, conversationId, cursor, limit);
    }

    public Messages getMessages(String trainingId, String conversationId) {
        return Inference.getConversationMessages(aiId, trainingId, conversationId);
    }

    public TrainingInfoList getTrainingList() {
        return Inference.getTrainingList(aiId);
    }

    public PostTrainResult train() {
        return Trainer.postTrain(aiId);
    }

    public TrainingInfo getTrainingInfo(String trainingId) {
        return Trainer.getTrainingInfo(aiId, trainingId);
    }

    public Flux<String> sendChatCompletions(String conversationId, String content,
                                            ChatCallBack callBack)
        throws AiServerException {
        return Inference.sendChatCompletions(aiId, conversationId, content, callBack);
    }
}
