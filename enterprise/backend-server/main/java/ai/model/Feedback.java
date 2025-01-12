package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.entity.AiConversationVoteEntity;
import com.apitable.shared.clock.spring.ClockManager;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * feedback object.
 */
@Data
public class Feedback {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String aiId;
    private String trainingId;
    private String conversationId;
    private Integer messageIndex;
    private Integer isLike;
    private String comment;
    private Integer state;
    private Long created;

    private String creator;
    private String avatar;
    private Integer color;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long memberId;
    private String aiModel;
    private String botType;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long conversationTime;

    public Feedback(AiConversationVoteEntity entity) {
        this.id = entity.getId();
        this.aiId = entity.getAiId();
        this.trainingId = entity.getTrainingId();
        this.conversationId = entity.getConversationId();
        this.messageIndex = entity.getMessageIndex();
        this.isLike = entity.getIsLike();
        this.comment = entity.getComment();
        this.state = entity.getState();
        if (entity.getCreatedAt() != null) {
            this.created = ClockManager.me().convertUnixTimeToMillis(entity.getCreatedAt());
        }
    }
}
