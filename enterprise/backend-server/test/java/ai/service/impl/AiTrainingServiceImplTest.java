package com.apitable.enterprise.ai.service.impl;

import static com.apitable.enterprise.ai.server.Inference.getTrainingList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import com.apitable.enterprise.AbstractApitableSaasIntegrationTest;
import com.apitable.enterprise.ai.model.TrainingInfoVO;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.model.TrainingInfo;
import com.apitable.enterprise.ai.server.model.TrainingInfoList;
import com.apitable.mock.bean.MockUserSpace;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class AiTrainingServiceImplTest extends AbstractApitableSaasIntegrationTest {

    @Test
    void testGetTrainingList() {
        try (
            MockedStatic<Inference> inference = mockStatic(Inference.class);
        ) {
            MockUserSpace userSpace = createSingleUserAndSpace();
            String aiNodeId = createAiNode(userSpace.getSpaceId());
            TrainingInfoList trainingInfoList = new TrainingInfoList();
            trainingInfoList.add(mockTrainingInfo(aiNodeId));
            trainingInfoList.add(mockTrainingInfo(aiNodeId));
            inference.when(() -> getTrainingList(aiNodeId)).thenReturn(trainingInfoList);
            List<TrainingInfoVO> trainingList = iAiTrainingService.getTrainingList(aiNodeId);
            assertThat(trainingList).isNotEmpty();
        }
    }

    private TrainingInfo mockTrainingInfo(String aiId) {
        Random random = new Random();
        TrainingInfo trainingInfo = new TrainingInfo();
        trainingInfo.setAiId(aiId);
        trainingInfo.setTrainingId("training_" + random.nextInt(10));
        trainingInfo.setStatus(TrainingStatus.TRAINING);
        trainingInfo.setInfo("info");
        OffsetDateTime offsetDateTime =
            OffsetDateTime.of(2023, 9, 26, 10, 19, 28, 0, ZoneOffset.UTC);
        trainingInfo.setStartedAt(offsetDateTime.toInstant().toEpochMilli() / 1000);
        trainingInfo.setFinishedAt(offsetDateTime.plusMinutes(5).toInstant().toEpochMilli() / 1000);
        trainingInfo.setDataSources(Lists.newArrayList());
        return trainingInfo;
    }
}
