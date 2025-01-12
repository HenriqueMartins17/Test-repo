package com.apitable.enterprise.ai.service.impl;

import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.TrainingInfoVO;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.Training;
import com.apitable.enterprise.ai.server.model.TrainingInfoList;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.ai.service.IAiTrainingService;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * AI training service implementation.
 *
 * @author Shawn Deng
 */
@Service
public class AiTrainingServiceImpl implements IAiTrainingService {

    @Resource
    private IAiService iAiService;

    @Override
    public List<TrainingInfoVO> getTrainingList(String aiId) {
        AiModel aiModel = iAiService.getAiModelOfAiId(aiId);
        TrainingInfoList trainingInfoList = Inference.getTrainingList(aiId);
        return trainingInfoList.stream().reduce(
            new ArrayList<>(trainingInfoList.size()),
            (acc, trainingInfo) -> {
                acc.add(new TrainingInfoVO(aiId, aiModel, new Training(trainingInfo)));
                return acc;
            },
            (acc, items) -> {
                acc.addAll(items);
                return acc;
            }
        );
    }
}
