package com.apitable.enterprise.ai.service.impl;

import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.mapper.AiNodeMapper;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.ai.service.IAiNodeService;
import com.apitable.enterprise.airagent.model.training.DataSource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ai datasheet service implements.
 *
 * @author Shawn Deng
 */
@Service
public class AiNodeServiceImpl extends ServiceImpl<AiNodeMapper, AiNodeEntity>
    implements IAiNodeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(String aiId, AiTrainingDataSource dataSource) {
        final int currentVersion = getLatestVersionByAiId(aiId);
        // delete old data source
        deleteByAiId(aiId);
        // insert new ai node rel data
        AiNodeEntity aiNodeEntity = new AiNodeEntity();
        aiNodeEntity.setAiId(aiId);
        aiNodeEntity.setNodeId(dataSource.getNodeId());
        aiNodeEntity.setNodeType(dataSource.getNodeType().getNodeType());
        aiNodeEntity.setSetting(dataSource.getSetting().toJson());
        aiNodeEntity.setVersion(currentVersion + 1);
        save(aiNodeEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(String aiId, List<DataSource> dataSources) {
        if (dataSources == null || dataSources.isEmpty()) {
            return;
        }
        List<AiNodeEntity> aiNodeEntities = new ArrayList<>();
        dataSources.forEach(dataSource -> {
            AiNodeEntity aiNodeEntity = new AiNodeEntity();
            aiNodeEntity.setAiId(aiId);
            aiNodeEntity.setType(dataSource.getType().getValue());
            aiNodeEntity.setSetting(dataSource.toSettingRawJson());
            aiNodeEntities.add(aiNodeEntity);
        });
        saveBatch(aiNodeEntities);
    }

    @Override
    public List<AiNodeEntity> listByAiId(String aiId) {
        QueryWrapper<AiNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ai_id", aiId).orderByDesc("created_at");
        return list(queryWrapper);
    }

    @Override
    public long getCountByAiId(String aiId) {
        QueryWrapper<AiNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ai_id", aiId);
        return count(queryWrapper);
    }

    @Override
    public Integer getLatestVersionByAiId(String aiId) {
        QueryWrapper<AiNodeEntity> queryWrapper = new QueryWrapper<AiNodeEntity>()
            .eq("ai_id", aiId)
            .orderByDesc("version");
        AiNodeEntity aiNodeEntity = getOne(queryWrapper, false);
        return (null == aiNodeEntity) ? 0 : aiNodeEntity.getVersion();
    }

    @Override
    public void deleteByAiId(String aiId) {
        QueryWrapper<AiNodeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ai_id", aiId);
        remove(queryWrapper);
    }
}
