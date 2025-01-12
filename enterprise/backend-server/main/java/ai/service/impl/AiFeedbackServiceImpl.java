package com.apitable.enterprise.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.AiConversationEntity;
import com.apitable.enterprise.ai.entity.AiConversationVoteEntity;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.mapper.FeedbackMapper;
import com.apitable.enterprise.ai.model.Feedback;
import com.apitable.enterprise.ai.model.FeedbackPagination;
import com.apitable.enterprise.ai.model.FeedbackQuery;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiFeedbackService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.organization.entity.MemberEntity;
import com.apitable.organization.service.IMemberService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * feedback service impl.
 */
@Service
public class AiFeedbackServiceImpl extends ServiceImpl<FeedbackMapper, AiConversationVoteEntity>
    implements IAiFeedbackService {

    @Resource
    private IAiService iAiService;

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMemberService iMemberService;

    @Override
    public String getAiIdById(Long id) {
        AiConversationVoteEntity entity = getById(id);
        return entity.getAiId();
    }

    @Override
    public PageInfo<Feedback> pagination(String aiId, String conversationId, FeedbackQuery query) {
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        QueryWrapper<AiConversationVoteEntity> queryWrapper =
            new QueryWrapper<AiConversationVoteEntity>()
                .eq("ai_id", aiId)
                .eq("conversation_id", conversationId);
        if (query.getState() != null) {
            queryWrapper = queryWrapper.eq("state", query.getState());
        }
        if (query.getSearch() != null) {
            queryWrapper = queryWrapper.like("comment", query.getSearch());
        }
        return pagination(aiId, queryWrapper, query);
    }

    @Override
    public PageInfo<Feedback> pagination(String aiId, FeedbackQuery query) {
        QueryWrapper<AiConversationVoteEntity> queryWrapper =
            new QueryWrapper<AiConversationVoteEntity>()
                .eq("ai_id", aiId);
        if (query.getState() != null) {
            queryWrapper = queryWrapper.eq("state", query.getState());
        }
        if (query.getSearch() != null) {
            queryWrapper = queryWrapper.like("comment", query.getSearch());
        }
        return pagination(aiId, queryWrapper, query);
    }

    private PageInfo<Feedback> pagination(String aiId,
                                          QueryWrapper<AiConversationVoteEntity> queryWrapper,
                                          FeedbackQuery query) {
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        Page<AiConversationVoteEntity> page = Page.of(query.getPageNum(), query.getPageSize());
        page.addOrder(OrderItem.desc("created_at"));
        IPage<AiConversationVoteEntity> pageInfo = page(page, queryWrapper);
        List<Long> userIds =
            pageInfo.getRecords().stream().map(AiConversationVoteEntity::getCreatedBy)
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toList());
        List<String> conversationIds =
            pageInfo.getRecords().stream().map(AiConversationVoteEntity::getConversationId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        List<AiConversationEntity> conversationEntities =
            iAiConversationService.getByConversationIds(conversationIds);
        Map<String, AiConversationEntity> conversationEntityMap =
            conversationEntities.stream()
                .collect(Collectors.toMap(AiConversationEntity::getConversationId,
                    entity -> entity));
        List<UserEntity> userEntities =
            CollUtil.isNotEmpty(userIds) ? iUserService.listByIds(userIds) : new ArrayList<>();
        Map<Long, UserEntity> userEntityMap =
            userEntities.stream().collect(Collectors.toMap(UserEntity::getId, entity -> entity));
        List<MemberEntity> memberEntities =
            iMemberService.getByUserIds(aiEntity.getSpaceId(), userIds);
        Map<Long, MemberEntity> memberEntityMap =
            memberEntities.stream()
                .collect(Collectors.toMap(MemberEntity::getUserId, entity -> entity));
        List<Feedback> feedbackList = pageInfo.getRecords().stream().reduce(new ArrayList<>(),
            (acc, entity) -> {
                Feedback feedback = new Feedback(entity);
                UserEntity userEntity = userEntityMap.get(entity.getCreatedBy());
                if (userEntity != null) {
                    feedback.setCreator(userEntity.getNickName());
                    feedback.setAvatar(userEntity.getAvatar());
                    feedback.setColor(userEntity.getColor());
                }
                MemberEntity memberEntity = memberEntityMap.get(entity.getCreatedBy());
                if (memberEntity != null) {
                    feedback.setMemberId(memberEntity.getId());
                    feedback.setAiModel(aiEntity.getModel());
                    feedback.setBotType(aiEntity.getType());
                }
                AiConversationEntity conversationEntity =
                    conversationEntityMap.get(entity.getConversationId());
                if (conversationEntity != null) {
                    feedback.setConversationTime(ClockManager.me()
                        .convertUnixTimeToMillis(conversationEntity.getCreatedAt()));
                }
                acc.add(feedback);
                return acc;
            },
            (acc, items) -> {
                acc.addAll(items);
                return acc;
            });
        return PageHelper.build(pageInfo.getCurrent(), pageInfo.getSize(),
            pageInfo.getTotal(), feedbackList);
    }

    @Override
    public FeedbackPagination paginationQuery(String aiId, FeedbackQuery query) {
        AiEntity aiEntity = iAiService.getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        QueryWrapper<AiConversationVoteEntity> queryWrapper =
            new QueryWrapper<AiConversationVoteEntity>()
                .eq("ai_id", aiId);
        if (query.getState() != null) {
            queryWrapper = queryWrapper.eq("state", query.getState());
        }
        if (query.getSearch() != null) {
            queryWrapper = queryWrapper.like("comment", query.getSearch());
        }
        Page<AiConversationVoteEntity> page = Page.of(query.getPageNum(), query.getPageSize());
        page.addOrder(OrderItem.desc("created_at"));
        Page<AiConversationVoteEntity> pageInfo = page(page, queryWrapper);
        List<Long> userIds =
            pageInfo.getRecords().stream().map(AiConversationVoteEntity::getCreatedBy)
                .filter(id -> id != null && id != 0L)
                .collect(Collectors.toList());
        List<String> conversationIds =
            pageInfo.getRecords().stream().map(AiConversationVoteEntity::getConversationId)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
        List<AiConversationEntity> conversationEntities =
            iAiConversationService.getByConversationIds(conversationIds);
        Map<String, AiConversationEntity> conversationEntityMap =
            conversationEntities.stream()
                .collect(Collectors.toMap(AiConversationEntity::getConversationId,
                    entity -> entity));
        List<UserEntity> userEntities =
            CollUtil.isNotEmpty(userIds) ? iUserService.listByIds(userIds) : new ArrayList<>();
        Map<Long, UserEntity> userEntityMap =
            userEntities.stream().collect(Collectors.toMap(UserEntity::getId, entity -> entity));
        List<MemberEntity> memberEntities =
            iMemberService.getByUserIds(aiEntity.getSpaceId(), userIds);
        Map<Long, MemberEntity> memberEntityMap =
            memberEntities.stream()
                .collect(Collectors.toMap(MemberEntity::getUserId, entity -> entity));
        List<Feedback> feedbackList = pageInfo.getRecords().stream().reduce(new ArrayList<>(),
            (acc, entity) -> {
                Feedback feedback = new Feedback(entity);
                UserEntity userEntity = userEntityMap.get(entity.getCreatedBy());
                if (userEntity != null) {
                    feedback.setCreator(userEntity.getNickName());
                    feedback.setAvatar(userEntity.getAvatar());
                    feedback.setColor(userEntity.getColor());
                }
                MemberEntity memberEntity = memberEntityMap.get(entity.getCreatedBy());
                if (memberEntity != null) {
                    feedback.setMemberId(memberEntity.getId());
                    feedback.setAiModel(aiEntity.getModel());
                    feedback.setBotType(aiEntity.getType());
                }
                AiConversationEntity conversationEntity =
                    conversationEntityMap.get(entity.getConversationId());
                if (conversationEntity != null) {
                    feedback.setConversationTime(ClockManager.me()
                        .convertUnixTimeToMillis(conversationEntity.getCreatedAt()));
                }
                acc.add(feedback);
                return acc;
            },
            (acc, items) -> {
                acc.addAll(items);
                return acc;
            });
        return new FeedbackPagination(pageInfo.getTotal(), pageInfo.getSize(),
            pageInfo.getCurrent(), feedbackList);
    }

    @Override
    public List<Feedback> getAiFeedbackByConversationId(String aiId, String conversationId) {
        QueryWrapper<AiConversationVoteEntity> queryWrapper =
            new QueryWrapper<AiConversationVoteEntity>()
                .eq("ai_id", aiId)
                .eq("conversation_id", conversationId);
        List<AiConversationVoteEntity> entities = list(queryWrapper);
        return entities.stream().reduce(new ArrayList<>(),
            (acc, entity) -> {
                acc.add(new Feedback(entity));
                return acc;
            },
            (acc, items) -> {
                acc.addAll(items);
                return acc;
            });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Feedback create(String spaceId, String aiId, String trainingId, String conversationId,
                           Integer messageIndex, Integer isLike, String comment, Long createdBy) {
        AiConversationVoteEntity entity = new AiConversationVoteEntity();
        entity.setSpaceId(spaceId);
        entity.setAiId(aiId);
        entity.setTrainingId(trainingId);
        entity.setConversationId(conversationId);
        entity.setMessageIndex(messageIndex);
        entity.setIsLike(isLike);
        entity.setComment(comment);
        if (createdBy != null) {
            entity.setCreatedBy(createdBy);
            entity.setUpdatedBy(createdBy);
        } else {
            entity.setCreatedBy(0L);
            entity.setUpdatedBy(0L);
        }
        save(entity);
        return new Feedback(entity);
    }

    @Override
    public boolean updateState(Long id, Integer state) {
        AiConversationVoteEntity updateEntity = new AiConversationVoteEntity();
        updateEntity.setId(id);
        updateEntity.setState(state);
        return updateById(updateEntity);
    }
}
