package com.apitable.enterprise.ai.model;

import java.util.List;
import lombok.Data;

/**
 * Training Predict Params.
 */
@Data
public class TrainingPredictParams {

    private List<AiUpdateParams.DataSourceParam> dataSources;
}
