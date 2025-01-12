package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.CopilotConversationEntity;
import com.apitable.enterprise.ai.model.ConversationCreatePrams;
import com.apitable.enterprise.ai.model.CopilotConversation;
import com.apitable.enterprise.ai.model.LatestConversation;
import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * copilot conversation service.
 */
public interface ICopilotConversationService extends IService<CopilotConversationEntity> {

    /**
     * retrieve latest conversation.
     *
     * @param userId  user id
     * @param spaceId space id
     * @return latest conversation
     */
    LatestConversation retrieveLatestConversationId(Long userId, String spaceId);

    /**
     * retrieve copilot conversation.
     *
     * @param conversationId conversation id
     * @return conversation object
     */
    CopilotConversation retrieve(String conversationId);

    /**
     * create copilot conversation.
     *
     * @param spaceId space id
     * @param title   title
     * @param type    copilot type
     * @return conversation id
     */
    String create(String spaceId, String title, CopilotAssistantType type);

    /**
     * get user last conversation by space id.
     *
     * @param userId  user id
     * @param spaceId space id
     * @return conversation entity
     */
    CopilotConversationEntity getLastConversationBySpaceIdAndCreatedBy(Long userId,
                                                                       String spaceId);

    /**
     * get user-specific conversations.
     *
     * @param conversationId conversation id
     * @param createdBy      user id
     * @return copilot conversation entity
     */
    CopilotConversationEntity getUserConversation(String conversationId,
                                                  Long createdBy);

    /**
     * update conversation.
     *
     * @param conversationId conversation id
     * @param params         params
     */
    void update(String conversationId, ConversationCreatePrams params);
}
