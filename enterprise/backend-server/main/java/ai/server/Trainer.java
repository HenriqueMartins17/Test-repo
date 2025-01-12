package com.apitable.enterprise.ai.server;

import com.apitable.enterprise.ai.server.model.PostTrainResult;
import com.apitable.enterprise.ai.server.model.TrainPredictInfoList;
import com.apitable.enterprise.ai.server.model.TrainPredictParams;
import com.apitable.enterprise.ai.server.model.TrainingInfo;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * AI train request remote.
 *
 * @author Shawn Deng
 */
@Getter
@Setter
public class Trainer extends ApiOperation {

    private static final String POST_TRAIN = "/ai/trainers/%s/train";

    private static final String GET_TRAINING_INFO = "/ai/trainers/%s/trainings/%s";

    private static final String PREDICT = "/ai/trainers/predict";


    /**
     * request train ai node.
     *
     * @param aiId ai unique id
     * @return Trainer
     * @throws AiServerException ai server call exception
     */
    public static PostTrainResult postTrain(String aiId) throws AiServerException {
        return post(String.format(POST_TRAIN, aiId), PostTrainResult.class);
    }

    public static TrainingInfo getTrainingInfo(String aiId, String trainingId)
        throws AiServerException {
        return get(String.format(GET_TRAINING_INFO, aiId, trainingId), TrainingInfo.class);
    }

    public static TrainPredictInfoList predict(TrainPredictParams params) throws AiServerException {
        List<TrainPredictParams.DataSource> dataSources = params.getDataSources();
        return post(PREDICT, dataSources, TrainPredictInfoList.class);
    }
}
