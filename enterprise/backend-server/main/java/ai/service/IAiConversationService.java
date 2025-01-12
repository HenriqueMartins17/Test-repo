package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiConversationEntity;
import com.apitable.enterprise.ai.model.Conversation;
import com.apitable.enterprise.ai.model.ConversationOrigin;
import com.apitable.shared.util.page.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import org.springframework.data.domain.PageRequest;

/**
 * ai conversation service.
 */
public interface IAiConversationService extends IService<AiConversationEntity> {

    /**
     * create conversation.
     *
     * @param aiId      ai id
     * @param title     conversation simple title
     * @param origin    conversation origin
     * @param createdBy created by
     * @return conversation id
     */
    String create(String aiId, String title, ConversationOrigin origin, Long createdBy);

    /**
     * get id by conversation id.
     *
     * @param conversationId conversation id
     * @return id
     */
    Long getIdByConversationId(String conversationId);

    /**
     * get last conversation by ai id and created by.
     *
     * @param aiId   ai id
     * @param userId user id
     * @return AiConversationEntity
     */
    AiConversationEntity getLastConversationByAiIdAndCreatedBy(String aiId, Long userId);

    /**
     * get by conversation ids.
     *
     * @param conversationIds conversation ids
     * @return AiConversationEntity list
     */
    List<AiConversationEntity> getByConversationIds(List<String> conversationIds);

    /**
     * get by conversation id.
     *
     * @param conversationId conversation id
     * @return AiConversationEntity
     */
    AiConversationEntity getByConversationId(String conversationId);

    /**
     * get ai id by conversation id.
     *
     * @param conversationId conversation id
     * @return ai id
     */
    String getAiIdByConversationId(String conversationId);

    /**
     * check conversation.
     *
     * @param conversationId conversation id
     */
    void checkConversation(String conversationId);

    /**
     * retrieve conversation.
     *
     * @param conversationId conversation id
     * @return conversation
     */
    Conversation retrieve(String conversationId);

    /**
     * pagination query with user.
     *
     * @param userId      user id
     * @param aiId        ai id
     * @param pageRequest pageable object
     * @return pagination result
     */
    PageInfo<Conversation> userPagination(Long userId, String aiId,
                                          PageRequest pageRequest);

    /**
     * pagination query.
     *
     * @param aiId        ai id
     * @param pageRequest pageable object
     * @return pagination result
     */
    PageInfo<Conversation> pagination(String aiId, PageRequest pageRequest);
}
