package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiConversationVoteEntity;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackPagination;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.shared.util.page.PageInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * feedback service.
 */
public interface IAiFeedbackService extends IService<AiConversationVoteEntity> {

    /**
     * get ai id by id.
     *
     * @param id id
     * @return ai id
     */
    String getAiIdById(Long id);

    /**
     * pagination with conversation.
     *
     * @param aiId           ai id
     * @param conversationId conversation id
     * @param query          query condition
     * @return feedback pagination
     */
    PageInfo<Feedback> pagination(String aiId, String conversationId, FeedbackQuery query);

    /**
     * pagination.
     *
     * @param aiId  ai id
     * @param query query condition
     * @return feedback pagination
     */
    PageInfo<Feedback> pagination(String aiId, FeedbackQuery query);

    /**
     * pagination query.
     *
     * @param aiId  ai id
     * @param query query condition
     * @return feedback pagination
     */
    @Deprecated(since = "1.8.0", forRemoval = true)
    FeedbackPagination paginationQuery(String aiId, FeedbackQuery query);

    /**
     * get feedback by conversation id.
     *
     * @param aiId           ai id
     * @param conversationId conversation id
     * @return feedback list
     */
    @Deprecated(since = "1.8.0", forRemoval = true)
    List<Feedback> getAiFeedbackByConversationId(String aiId, String conversationId);

    /**
     * create feedback.
     *
     * @param spaceId        space id
     * @param aiId           ai id
     * @param trainingId     training id
     * @param conversationId conversation id
     * @param messageIndex   message index
     * @param isLike         is like
     * @param comment        comment
     * @param createdBy      created by
     * @return feedback
     */
    Feedback create(String spaceId, String aiId, String trainingId, String conversationId,
                    Integer messageIndex, Integer isLike, String comment, Long createdBy);

    /**
     * update feedback state.
     *
     * @param id    id
     * @param state state
     * @return boolean
     */
    boolean updateState(Long id, Integer state);
}
