package com.apitable.enterprise.ai.model;

import com.apitable.enterprise.ai.entity.CopilotConversationEntity;
import com.apitable.enterprise.ai.server.Copilots;
import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.apitable.shared.clock.spring.ClockManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * the conversation object of copilot.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class CopilotConversation {

    /**
     * conversation id.
     */
    private String id;

    /**
     * conversation title.
     */
    private String title;

    private CopilotAssistantType type;

    private String model;

    private String prologue;

    /**
     * conversation created time.
     */
    private Long created;

    public static CopilotConversation of(CopilotConversationEntity entity) {
        var conversation = new CopilotConversation();
        conversation.setId(entity.getConversationId());
        conversation.setTitle(entity.getTitle());
        CopilotAssistantType copilotAssistantType = CopilotAssistantType.of(entity.getType());
        conversation.setType(copilotAssistantType);
        conversation.setPrologue(Copilots.getPrologue(copilotAssistantType));
        conversation.setModel(entity.getModel());
        conversation.setCreated(
            ClockManager.me().convertUnixTimeToMillis(entity.getCreatedAt()));
        return conversation;
    }
}
