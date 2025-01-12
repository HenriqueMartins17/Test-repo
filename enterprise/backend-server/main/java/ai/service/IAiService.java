package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.model.Ai;
import com.apitable.enterprise.ai.model.AiLoaderConfig;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.model.AiUpdateParams;
import com.apitable.enterprise.ai.model.TrainCallback;
import com.apitable.enterprise.ai.model.TrainingPredictResult;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.server.model.Training;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * ai service interface.
 *
 * @author Shawn Deng
 */
public interface IAiService extends IService<AiEntity> {

    /**
     * create ai chatBot.
     *
     * @param aiObject ai object
     */
    void create(AiObject aiObject);

    /**
     * batch delete ai.
     *
     * @param aiIds ai unique id list
     */
    void deleteAi(List<String> aiIds);

    /**
     * retrieve entity by ai id.
     *
     * @param aiId ai unique id
     * @return AiEntity
     */
    AiEntity getByAiId(String aiId);

    /**
     * retrieve ai model by ai id.
     *
     * @param aiId ai unique id
     * @return AiModel
     */
    AiModel getAiModelOfAiId(String aiId);

    /**
     * check ai exist.
     *
     * @param aiId ai id
     */
    void checkExist(String aiId);

    /**
     * retrieve batch by ai id list.
     *
     * @param aiIds ai id list
     * @return ai entity list
     */
    List<AiEntity> listByAiIds(List<String> aiIds);

    /**
     * get ai by ai id.
     *
     * @param aiId   ai id
     * @param config ai loader config
     * @return ai
     */
    Ai getAi(String aiId, AiLoaderConfig config);

    /**
     * update ai.
     *
     * @param aiId         ai id
     * @param updateParams update params
     */
    void updateAi(String aiId, AiUpdateParams updateParams);

    /**
     * get train predict result.
     *
     * @param aiId   ai id
     * @param params data source params
     * @return TrainingPredictResult
     */
    TrainingPredictResult getTrainPredict(String aiId, List<AiUpdateParams.DataSourceParam> params);

    /**
     * train ai with different data source.
     *
     * @param aiId          ai id
     * @param trainCallback train callback
     */
    void trainAi(String aiId, TrainCallback trainCallback);

    /**
     * train ai.
     *
     * @param aiId          ai id
     * @param trainCallback train callback
     */
    void train(String aiId, TrainCallback trainCallback);

    /**
     * get latest training status.
     *
     * @param aiId ai id
     * @return TrainingStatus
     */
    TrainingStatus getLatestTrainingStatus(String aiId);

    /**
     * create query transaction.
     *
     * @param aiId     ai id
     * @param training training
     * @param byWho    user id
     */
    void createQueryTransaction(String aiId, Training training, Long byWho);
}
