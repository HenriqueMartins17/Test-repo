package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiNodeEntity;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.airagent.model.training.DataSource;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * ai datasheet service interface.
 *
 * @author Shawn Deng
 */
public interface IAiNodeService extends IService<AiNodeEntity> {

    /**
     * create AI node relation.
     *
     * @param aiId       ai id
     * @param dataSource data source
     */
    void create(String aiId, AiTrainingDataSource dataSource);

    /**
     * create AI node relation batch.
     *
     * @param aiId        ai id
     * @param dataSources data sources
     */
    void createBatch(String aiId, List<DataSource> dataSources);

    /**
     * get list by ai id.
     *
     * @param aiId ai id
     * @return list of ai node entity
     */
    List<AiNodeEntity> listByAiId(String aiId);

    /**
     * get total count by ai id.
     * @param aiId ai id
     * @return count
     */
    long getCountByAiId(String aiId);

    /**
     * get latest version by ai id.
     *
     * @param aiId ai id
     * @return latest version
     */
    Integer getLatestVersionByAiId(String aiId);

    /**
     * delete by ai id.
     *
     * @param aiId ai id
     */
    void deleteByAiId(String aiId);
}
