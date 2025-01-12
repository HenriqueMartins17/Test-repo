package com.apitable.enterprise.ai.service.impl;

import static com.apitable.enterprise.ai.exception.CopilotException.COPILOT_CONVERSATION_NOT_FOUND;

import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.CopilotConversationEntity;
import com.apitable.enterprise.ai.mapper.CopilotConversationMapper;
import com.apitable.enterprise.ai.model.ConversationCreatePrams;
import com.apitable.enterprise.ai.model.CopilotConversation;
import com.apitable.enterprise.ai.model.LatestConversation;
import com.apitable.enterprise.ai.server.model.CopilotAssistantType;
import com.apitable.enterprise.ai.service.ICopilotConversationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * copilot conversation service implementation.
 */
@Service
public class CopilotConversationServiceImpl
    extends ServiceImpl<CopilotConversationMapper, CopilotConversationEntity>
    implements ICopilotConversationService {

    @Override
    public LatestConversation retrieveLatestConversationId(Long userId, String spaceId) {
        var latestConversation =
            getLastConversationBySpaceIdAndCreatedBy(userId, spaceId);
        if (latestConversation != null) {
            return new LatestConversation(false, latestConversation.getConversationId());
        }
        var entity = new CopilotConversationEntity();
        entity.setSpaceId(spaceId);
        entity.setConversationId(UUID.randomUUID().toString());
        entity.setType(CopilotAssistantType.HELP.getValue());
        entity.setModel("gpt-3.5-turbo-1106");
        save(entity);
        return new LatestConversation(true, entity.getConversationId());
    }

    private CopilotConversationEntity getByConversationId(String conversationId) {
        var queryWrapper = new QueryWrapper<CopilotConversationEntity>()
            .eq("conversation_id", conversationId);
        return getOne(queryWrapper, false);
    }

    @Override
    public CopilotConversation retrieve(String conversationId) {
        var conversationEntity = getByConversationId(conversationId);
        return CopilotConversation.of(conversationEntity);
    }

    @Override
    public String create(String spaceId, String title, CopilotAssistantType type) {
        var conversationEntity = new CopilotConversationEntity();
        conversationEntity.setSpaceId(spaceId);
        conversationEntity.setConversationId(UUID.randomUUID().toString());
        conversationEntity.setTitle(title);
        conversationEntity.setType(type.getValue());
        conversationEntity.setModel("gpt-3.5-turbo-1106");
        save(conversationEntity);
        return conversationEntity.getConversationId();
    }

    @Override
    public CopilotConversationEntity getLastConversationBySpaceIdAndCreatedBy(Long userId,
                                                                              String spaceId) {
        var queryWrapper = new QueryWrapper<CopilotConversationEntity>()
            .eq("space_id", spaceId)
            .eq("created_by", userId)
            .orderByDesc("created_at");
        return getOne(queryWrapper, false);
    }

    @Override
    public CopilotConversationEntity getUserConversation(String conversationId,
                                                         Long createdBy) {
        var queryWrapper = new QueryWrapper<CopilotConversationEntity>()
            .eq("conversation_id", conversationId)
            .eq("created_by", createdBy);
        return getOne(queryWrapper);
    }

    @Override
    public void update(String conversationId, ConversationCreatePrams params) {
        CopilotConversationEntity conversationEntity = getByConversationId(conversationId);
        if (conversationEntity == null) {
            throw new BusinessException(COPILOT_CONVERSATION_NOT_FOUND);
        }
        CopilotConversationEntity updatedEntity = new CopilotConversationEntity();
        updatedEntity.setId(conversationEntity.getId());
        updatedEntity.setType(params.getType().getValue());
        updateById(updatedEntity);
    }
}
