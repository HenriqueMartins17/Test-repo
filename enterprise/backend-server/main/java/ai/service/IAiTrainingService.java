package com.apitable.enterprise.ai.service;

import com.apitable.enterprise.ai.model.TrainingInfoVO;
import java.util.List;

/**
 * AI training service.
 *
 * @author Shawn Deng
 */
public interface IAiTrainingService {

    /**
     * get training list.
     *
     * @param aiId ai id
     * @return training list
     */
    List<TrainingInfoVO> getTrainingList(String aiId);
}
