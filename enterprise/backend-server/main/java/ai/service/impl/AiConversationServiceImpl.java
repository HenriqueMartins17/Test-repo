package com.apitable.enterprise.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.AiConversationEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.mapper.AiConversationMapper;
import com.apitable.enterprise.ai.model.Conversation;
import com.apitable.enterprise.ai.model.ConversationOrigin;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.AiInfo;
import com.apitable.enterprise.ai.server.model.Training;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * ai conversation service implements.
 */
@Service
public class AiConversationServiceImpl
    extends ServiceImpl<AiConversationMapper, AiConversationEntity>
    implements IAiConversationService {

    @Resource
    private IUserService iUserService;

    @Resource
    private IAiCreditTransactionService iAiCreditTransactionService;

    @Override
    public String create(String aiId, String title, ConversationOrigin origin, Long createdBy) {
        AiInfo aiInfo = Inference.getAiInfo(aiId);
        Training latestSuccessTraining =
            aiInfo != null && !aiInfo.isNull() ? aiInfo.latestSuccessTraining() : null;
        String trainingId = latestSuccessTraining != null ? latestSuccessTraining.getId() : null;
        AiConversationEntity conversationEntity = new AiConversationEntity();
        conversationEntity.setAiId(aiId);
        conversationEntity.setTrainingId(trainingId);
        conversationEntity.setConversationId(UUID.randomUUID().toString());
        conversationEntity.setTitle(title);
        conversationEntity.setOrigin(origin.getValue());
        conversationEntity.setOriginType(origin.getValue());
        if (createdBy != null) {
            conversationEntity.setCreatedBy(createdBy);
            conversationEntity.setUpdatedBy(createdBy);
        } else {
            conversationEntity.setCreatedBy(0L);
            conversationEntity.setUpdatedBy(0L);
        }
        save(conversationEntity);
        return conversationEntity.getConversationId();
    }

    @Override
    public Long getIdByConversationId(String conversationId) {
        QueryWrapper<AiConversationEntity> queryWrapper = new QueryWrapper<AiConversationEntity>()
            .eq("conversation_id", conversationId);
        AiConversationEntity conversationEntity = getOne(queryWrapper);
        return conversationEntity.getId();
    }

    @Override
    public AiConversationEntity getLastConversationByAiIdAndCreatedBy(String aiId, Long userId) {
        QueryWrapper<AiConversationEntity> queryWrapper = new QueryWrapper<AiConversationEntity>()
            .eq("ai_id", aiId).eq("created_by", userId)
            .eq("origin", ConversationOrigin.INTERNAL.getValue())
            .orderByDesc("created_at");
        return getOne(queryWrapper, false);
    }

    @Override
    public List<AiConversationEntity> getByConversationIds(List<String> conversationIds) {
        if (conversationIds.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper<AiConversationEntity> queryWrapper = new QueryWrapper<AiConversationEntity>()
            .in("conversation_id", conversationIds);
        return list(queryWrapper);
    }

    @Override
    public AiConversationEntity getByConversationId(String conversationId) {
        QueryWrapper<AiConversationEntity> queryWrapper = new QueryWrapper<AiConversationEntity>()
            .eq("conversation_id", conversationId);
        return getOne(queryWrapper);
    }

    @Override
    public String getAiIdByConversationId(String conversationId) {
        AiConversationEntity conversationEntity = getByConversationId(conversationId);
        return conversationEntity.getAiId();
    }

    @Override
    public void checkConversation(String conversationId) {
        AiConversationEntity conversationEntity = getByConversationId(conversationId);
        if (conversationEntity == null) {
            throw new BusinessException(AiException.CONVERSATION_NOT_FOUND);
        }
    }

    @Override
    public Conversation retrieve(String conversationId) {
        AiConversationEntity entity = getByConversationId(conversationId);
        Conversation conversation = new Conversation();
        conversation.setId(entity.getConversationId());
        conversation.setAiId(entity.getAiId());
        conversation.setTitle(entity.getTitle());
        conversation.setTrainingId(entity.getTrainingId());
        conversation.setCreated(
            ClockManager.me().convertUnixTimeToMillis(entity.getCreatedAt()));
        conversation.setOrigin(entity.getOrigin());
        UserEntity userEntity = iUserService.getById(entity.getCreatedBy());
        if (userEntity != null) {
            conversation.setUser(
                Conversation.User.of(userEntity.getNickName(),
                    userEntity.getAvatar()));
        }
        BigDecimal amount = iAiCreditTransactionService
            .getTotalCreditTransactionAmountByConversationId(entity.getConversationId());
        conversation.setTransaction(new Conversation.Transaction(amount));
        return conversation;
    }

    @Override
    public PageInfo<Conversation> userPagination(Long userId, String aiId,
                                                 PageRequest pageRequest) {
        var queryWrapper = new QueryWrapper<AiConversationEntity>()
            .eq("ai_id", aiId)
            .eq("created_by", userId);
        return pagination(queryWrapper, pageRequest);
    }

    @Override
    public PageInfo<Conversation> pagination(String aiId, PageRequest pageRequest) {
        var queryWrapper = new QueryWrapper<AiConversationEntity>()
            .eq("ai_id", aiId);
        return pagination(queryWrapper, pageRequest);
    }

    private PageInfo<Conversation> pagination(QueryWrapper<AiConversationEntity> queryWrapper,
                                              PageRequest pageRequest) {
        Page<AiConversationEntity> page =
            Page.of(pageRequest.getPageNumber(), pageRequest.getPageSize());
        page.addOrder(OrderItem.desc("created_at"));
        IPage<AiConversationEntity> resultSet = page(page, queryWrapper);
        var records = resultSet.getRecords();
        List<Long> userIds =
            records.stream().map(AiConversationEntity::getCreatedBy)
                .filter(id -> id != null && id != 0L)
                .toList();
        List<UserEntity> userEntities =
            CollUtil.isNotEmpty(userIds) ? iUserService.listByIds(userIds) : new ArrayList<>();
        Map<Long, UserEntity> userEntityMap =
            userEntities.stream().collect(Collectors.toMap(UserEntity::getId, entity -> entity));
        List<Conversation> data = resultSet.getRecords().stream().reduce(
            new ArrayList<>(),
            (conversations, item) -> {
                Conversation conversation = new Conversation();
                conversation.setId(item.getConversationId());
                conversation.setAiId(item.getAiId());
                conversation.setTitle(item.getTitle());
                conversation.setTrainingId(item.getTrainingId());
                conversation.setCreated(
                    ClockManager.me().convertUnixTimeToMillis(item.getCreatedAt()));
                conversation.setOrigin(item.getOrigin());
                UserEntity userEntity = userEntityMap.get(item.getCreatedBy());
                if (userEntity != null) {
                    conversation.setUser(
                        Conversation.User.of(userEntity.getNickName(), userEntity.getAvatar()));
                }
                BigDecimal amount = iAiCreditTransactionService
                    .getTotalCreditTransactionAmountByConversationId(item.getConversationId());
                conversation.setTransaction(new Conversation.Transaction(amount));
                conversations.add(conversation);
                return conversations;
            },
            (conversations, items) -> {
                conversations.addAll(items);
                return conversations;
            });
        return PageHelper.build(resultSet.getCurrent(), resultSet.getSize(), resultSet.getTotal(),
            data);
    }

}
