package com.apitable.enterprise.ai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.credit.CreditConverter;
import com.apitable.enterprise.ai.credit.EmbeddingText;
import com.apitable.enterprise.ai.entity.AiConversationEntity;
import com.apitable.enterprise.ai.entity.AiCreditTransactionEntity;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.mapper.AiMapper;
import com.apitable.enterprise.ai.model.Ai;
import com.apitable.enterprise.ai.model.AiLoaderConfig;
import com.apitable.enterprise.ai.model.AiModel;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.model.AiTrainingDataSource;
import com.apitable.enterprise.ai.model.AiUpdateParams;
import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.model.TrainCallback;
import com.apitable.enterprise.ai.model.TrainingPredictResult;
import com.apitable.enterprise.ai.model.TrainingStatus;
import com.apitable.enterprise.ai.model.TransactionType;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.server.Trainer;
import com.apitable.enterprise.ai.server.model.AiInfo;
import com.apitable.enterprise.ai.server.model.DataSourceType;
import com.apitable.enterprise.ai.server.model.Messages;
import com.apitable.enterprise.ai.server.model.PostTrainResult;
import com.apitable.enterprise.ai.server.model.TrainPredictInfo;
import com.apitable.enterprise.ai.server.model.TrainPredictInfoList;
import com.apitable.enterprise.ai.server.model.TrainPredictParams;
import com.apitable.enterprise.ai.server.model.Training;
import com.apitable.enterprise.ai.service.IAiConversationService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.enterprise.ai.service.IAiNodeService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.enterprise.ai.service.IAiTrainingDataSourceService;
import com.apitable.enterprise.airagent.model.training.DataSources;
import com.apitable.enterprise.airagent.service.IDataSourceService;
import com.apitable.interfaces.ai.model.AiType;
import com.apitable.interfaces.ai.model.CreditInfo;
import com.apitable.shared.clock.spring.ClockManager;
import com.apitable.shared.exception.LimitException;
import com.apitable.shared.util.CollectionUtil;
import com.apitable.space.service.ISpaceService;
import com.apitable.workspace.service.IDatasheetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * ai service implements.
 *
 * @author Shawn Deng
 */
@Service
public class AiServiceImpl extends ServiceImpl<AiMapper, AiEntity> implements IAiService {

    @Resource
    private IAiNodeService iAiNodeService;

    @Resource
    private IAiTrainingDataSourceService iAiTrainingDataSourceService;

    @Resource
    private IDataSourceService iDataSourceService;

    @Resource
    private IAiConversationService iAiConversationService;

    @Resource
    private IAiCreditTransactionService iAiCreditTransactionService;

    @Resource
    private IDatasheetService iDatasheetService;

    @Resource
    private ISpaceService iSpaceService;

    @Override
    public void create(AiObject aiObject) {
        AiEntity aiEntity = new AiEntity();
        aiEntity.setSpaceId(aiObject.getSpaceId());
        aiEntity.setNodeId(aiObject.getAiId());
        aiEntity.setAiId(aiObject.getAiId());
        if (aiObject.getType() != null) {
            aiEntity.setType(aiObject.getType().getValue());
        }
        aiEntity.setName(aiObject.getName());
        aiEntity.setDescription(aiObject.getDescription());
        aiEntity.setPicture(aiObject.getPicture());
        aiEntity.setPrologue(aiObject.getPrologue());
        aiEntity.setPrompt(aiObject.getPrompt());
        aiEntity.setSetting(aiObject.getSetting());
        // save ai entity
        save(aiEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAi(List<String> aiIds) {
        List<AiEntity> aiEntities = listByAiIds(aiIds);
        if (!aiEntities.isEmpty()) {
            List<Long> ids = aiEntities.stream().map(AiEntity::getId).collect(Collectors.toList());
            removeBatchByIds(ids);
        }
    }

    @Override
    public AiEntity getByAiId(String aiId) {
        return getOne(new QueryWrapper<AiEntity>().eq("ai_id", aiId));
    }

    @Override
    public AiModel getAiModelOfAiId(String aiId) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        return AiModel.of(aiEntity.getModel());
    }

    @Override
    public void checkExist(String aiId) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
    }

    @Override
    public List<AiEntity> listByAiIds(List<String> aiIds) {
        return list(new QueryWrapper<AiEntity>().in("ai_id", aiIds));
    }

    private Map<String, Object> loadSetting(String aiId, String type) {
        if (StrUtil.isBlank(type)) {
            return new HashMap<>();
        }
        PureJson aiSettingSchema = Inference.getAiSetting(aiId);
        if (aiSettingSchema == null) {
            return new HashMap<>();
        }
        return aiSettingSchema.extractData();
    }

    @Override
    public Ai getAi(String aiId, AiLoaderConfig config) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        if (config == null) {
            config = AiLoaderConfig.create();
        }

        Map<String, Object> setting = loadSetting(aiId, aiEntity.getType());

        Ai ai = Ai.builder()
            .id(aiEntity.getAiId())
            .name(aiEntity.getName())
            .picture(aiEntity.getPicture())
            .description(aiEntity.getDescription())
            .type(aiEntity.getType())
            .setting(setting)
            .livemode(false)
            .created(ClockManager.me().convertUnixTimeToMillis(aiEntity.getCreatedAt()))
            .build();

        AiInfo aiInfo = Inference.getAiInfo(aiId);
        if (aiInfo != null && !aiInfo.isNull()) {
            boolean isLatestTrainingFailed;
            // set ai trained status
            ai.setIsTrained(aiInfo.hasTrained());
            // get latest training info object
            Training latestTraining = aiInfo.latestTraining();
            if (latestTraining != null) {
                isLatestTrainingFailed = latestTraining.getStatus() == TrainingStatus.FAILED;
                ai.setLatestTrainingId(latestTraining.getId());
                ai.setLatestTrainingStatus(latestTraining.getStatus());
                ai.setLatestTrainingCompletedAt(latestTraining.getCompletedAtMillis());
                AiConversationEntity currentConversation = config.isAnonymous()
                    ? null
                    : iAiConversationService.getLastConversationByAiIdAndCreatedBy(aiId,
                    config.getUserId());
                if (latestTraining.getStatus() == TrainingStatus.SUCCESS
                    && currentConversation != null) {
                    Messages messages =
                        Inference.getConversationMessages(aiId, latestTraining.getId(),
                            currentConversation.getConversationId());
                    if (CollectionUtil.isNotEmpty(messages)) {
                        ai.setCurrentConversationId(currentConversation.getConversationId());
                        ai.setCurrentConversationCreatedAt(ClockManager.me()
                            .convertUnixTimeToMillis(currentConversation.getCreatedAt()));
                    }
                }
            } else {
                ai.setIsTrained(false);
                // This can not be possible when ai info exists, currentTrainingId or lockingTrainingId must exist.
                log.error(
                    String.format(
                        "ai id %s info exists but no training info found, ai info schema: %s",
                        aiId,
                        JSONUtil.toJsonStr(aiInfo))
                );
                throw new RuntimeException("ai has not training info");
            }
            if (ai.getIsTrained() && isLatestTrainingFailed) {
                AiConversationEntity currentConversation = config.isAnonymous()
                    ? null
                    : iAiConversationService.getLastConversationByAiIdAndCreatedBy(aiId,
                    config.getUserId());
                if (currentConversation != null) {
                    ai.setCurrentConversationId(currentConversation.getConversationId());
                    ai.setCurrentConversationCreatedAt(ClockManager.me()
                        .convertUnixTimeToMillis(currentConversation.getCreatedAt()));
                }
            }
        } else {
            // may be not trained before, it is normal, ai info not found.
            ai.setIsTrained(false);
            ai.setLatestTrainingStatus(TrainingStatus.FAILED);
        }

        // load if qa bot
        if (AiType.QA.getValue().equals(aiEntity.getType())) {
            // assemble data sources
            AiTrainingDataSource dataSource =
                iAiTrainingDataSourceService.getDataSourceByAiId(aiId);
            if (dataSource != null) {
                ai.setDataSources(Collections.singletonList(dataSource));
                Long newestRevision = iDatasheetService.getRevisionByDstId(dataSource.getNodeId());
                ai.setDataSourcesUpdated(dataSource.isAfter(newestRevision));
            }
        }
        return ai;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAi(String aiId, AiUpdateParams updateParams) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        boolean updateAi = false;
        boolean switchFromQaType = false;
        AiEntity updateEntity = new AiEntity();

        if (StringUtils.hasText(updateParams.getDescription())
            && !updateParams.getDescription().equals(aiEntity.getDescription())) {
            updateEntity.setDescription(updateParams.getDescription());
            updateAi = true;
        }

        if (StringUtils.hasText(updateParams.getPicture())
            && !updateParams.getPicture().equals(aiEntity.getPicture())) {
            updateEntity.setPicture(updateParams.getPicture());
            updateAi = true;
        }

        if (MapUtil.isNotEmpty(updateParams.getSetting())) {
            JSONObject settingRequest = JSONUtil.parseObj(updateParams.getSetting());
            if (StrUtil.isNotBlank(settingRequest.getStr("type"))) {
                String type = settingRequest.getStr("type");
                updateEntity.setType(type);
                if (!type.equals(aiEntity.getType())) {
                    switchFromQaType = AiType.QA.getValue().equals(aiEntity.getType());
                }
                if (StrUtil.isBlank(settingRequest.getStr("model"))) {
                    // get default model
                    PureJson aiSettingSchema = Inference.getAiSetting(aiId, type);
                    if (aiSettingSchema == null) {
                        throw new BusinessException(AiException.AI_SETTING_NOT_SET);
                    }
                    Map<String, Object> data = aiSettingSchema.extractData();
                    updateEntity.setModel(MapUtil.getStr(data, "model"));
                }
            }
            if (StrUtil.isNotBlank(settingRequest.getStr("model"))) {
                updateEntity.setModel(MapUtil.getStr(settingRequest, "model"));
            }
            updateEntity.setSetting(settingRequest.toJSONString(0));
            updateAi = true;
        }

        if (updateAi) {
            updateEntity.setId(aiEntity.getId());
            updateById(updateEntity);
        }

        // check data source update available
        if (CollectionUtil.isNotEmpty(updateParams.getDataSources())) {
            // data source is not empty but switch from other mode to qa mode or not change qa mode
            if (!switchFromQaType) {
                // update data source
                String dataSourceNodeId =
                    updateParams.getDataSources().iterator().next().getNodeId();
                iAiTrainingDataSourceService.updateDataSource(aiId, dataSourceNodeId);
            } else {
                // switch to chat mode, delete old data source
                iAiNodeService.deleteByAiId(aiId);
            }
        } else {
            // data source is empty and switch from qa mode to other mode
            if (switchFromQaType) {
                // delete old data source
                iAiNodeService.deleteByAiId(aiId);
            }
        }
    }

    @Override
    public TrainingPredictResult getTrainPredict(String aiId,
                                                 List<AiUpdateParams.DataSourceParam> params) {
        List<TrainPredictParams.DataSource> dataSources = params.stream()
            .map(param -> TrainPredictParams.DataSource.builder()
                .type(DataSourceType.DATASHEET)
                .typeId(param.getNodeId())
                .build())
            .collect(Collectors.toList());
        TrainPredictParams predictParams = TrainPredictParams.builder()
            .dataSources(dataSources).build();
        TrainPredictInfoList trainPredictInfoList = Trainer.predict(predictParams);
        if (CollUtil.isEmpty(trainPredictInfoList)) {
            throw new BusinessException(AiException.TRAIN_PREDICT_FAIL);
        }
        TrainPredictInfo trainPredictInfo = trainPredictInfoList.iterator().next();
        // calculate consume credit
        AiModel aiModel = getAiModelOfAiId(aiId);
        EmbeddingText embeddingText = new EmbeddingText(aiModel,
            trainPredictInfo.getCharacters(), trainPredictInfo.getTokens());
        BigDecimal creditTrainingConsumed =
            CreditConverter.creditConsumedWithTraining(embeddingText);
        return new TrainingPredictResult(trainPredictInfo.getCharacters(),
            trainPredictInfo.getWords(), creditTrainingConsumed);
    }

    @Override
    public void trainAi(String aiId, TrainCallback trainCallback) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        if (StrUtil.isBlank(aiEntity.getType())) {
            throw new BusinessException(AiException.AI_TYPE_NOT_SET);
        }
        if (aiEntity.getType().equals(AiType.CHAT.getValue())) {
            trainAndReturn(aiId, trainCallback);
            return;
        }
        // QA Type Bot train
        DataSources dataSources = iDataSourceService.getDataSources(aiId);
        if (dataSources.isEmpty()) {
            throw new BusinessException(AiException.DATA_SOURCE_NOT_FOUND);
        }
        AiInfo aiInfo = Inference.getAiInfo(aiId);
        if (aiInfo == null || aiInfo.isNull()) {
            // train at first time
            trainAndReturn(aiId, trainCallback);
            return;
        }

        Training training = aiInfo.currentTrainingInfo();
        if (training != null && training.getStatus() == TrainingStatus.FAILED) {
            // retrain again when failed
            trainAndReturn(aiId, trainCallback);
            return;
        }
        // TODO check datasheet data source revision and update revision
        trainAndReturn(aiId, trainCallback);
    }

    @Override
    public void train(String aiId, TrainCallback trainCallback) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        if (StrUtil.isBlank(aiEntity.getType())) {
            throw new BusinessException(AiException.AI_TYPE_NOT_SET);
        }
        if (aiEntity.getType().equals(AiType.QA.getValue())) {
            AiTrainingDataSource dataSource = iAiTrainingDataSourceService
                .getDataSourceByAiId(aiId);
            if (dataSource == null) {
                throw new BusinessException(AiException.DATA_SOURCE_NOT_FOUND);
            }
            AiInfo aiInfo = Inference.getAiInfo(aiId);
            if (aiInfo == null || aiInfo.isNull()) {
                // directly train
                checkTrainCredit(aiEntity.getSpaceId(), aiId, dataSource);
                trainAndReturn(aiId, trainCallback);
                return;
            }
            // check whether ai train before
            if (!aiInfo.hasTrained()) {
                // directly train
                checkTrainCredit(aiEntity.getSpaceId(), aiId, dataSource);
                trainAndReturn(aiId, trainCallback);
                return;
            }
            // retrain if train error before
            Training training = aiInfo.currentTrainingInfo();
            if (training != null && training.getStatus() == TrainingStatus.FAILED) {
                // directly train
                checkTrainCredit(aiEntity.getSpaceId(), aiId, dataSource);
                trainAndReturn(aiId, trainCallback);
                return;
            }
            // only re-train when dataset revision is outdated
            Long newestRevision = iDatasheetService.getRevisionByDstId(dataSource.getNodeId());
            if (dataSource.isAfter(newestRevision)) {
                // update data source setting
                checkTrainCredit(aiEntity.getSpaceId(), aiId, dataSource);
                // update data source setting
                iAiTrainingDataSourceService.updateDataSourceSetting(aiId, dataSource.getNodeId());
                trainAndReturn(aiId, trainCallback);
            } else {
                checkTrainCredit(aiEntity.getSpaceId(), aiId, dataSource);
                trainAndReturn(aiId, trainCallback);
            }
        } else {
            trainAndReturn(aiId, trainCallback);
        }
    }

    private void trainAndReturn(String aiId, TrainCallback trainCallback) {
        PostTrainResult trainResult = Trainer.postTrain(aiId);
        if (trainResult == null) {
            throw new BusinessException(AiException.TRAIN_FAIL);
        }
        if (StrUtil.isBlank(trainResult.getNewTrainingId())) {
            throw new BusinessException(AiException.TRAIN_FAIL);
        }
        trainCallback.callback(trainResult);
    }

    @Override
    public TrainingStatus getLatestTrainingStatus(String aiId) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        AiInfo aiInfo = Inference.getAiInfo(aiId);
        return aiInfo != null && !aiInfo.isNull() ? aiInfo.latestTrainingStatus() : null;
    }

    private boolean trainIntercept(String spaceId, String aiId, AiTrainingDataSource dataSource) {
        // get data source
        List<TrainPredictParams.DataSource> dataSources =
            Stream.of(dataSource)
                .map(param -> TrainPredictParams.DataSource.builder()
                    .type(DataSourceType.DATASHEET)
                    .typeId(param.getNodeId())
                    .build())
                .collect(Collectors.toList());
        TrainPredictParams predictParams = TrainPredictParams.builder()
            .dataSources(dataSources).build();
        TrainPredictInfoList trainPredictInfoList = Trainer.predict(predictParams);
        if (CollUtil.isEmpty(trainPredictInfoList)) {
            throw new BusinessException(AiException.TRAIN_PREDICT_FAIL);
        }
        AiModel aiModel = getAiModelOfAiId(aiId);
        TrainPredictInfo trainPredictInfo = trainPredictInfoList.iterator().next();
        EmbeddingText embeddingText = new EmbeddingText(aiModel,
            trainPredictInfo.getCharacters(), trainPredictInfo.getTokens());
        BigDecimal creditTrainingConsumed =
            CreditConverter.creditConsumedWithTraining(embeddingText);
        CreditInfo creditInfo = iSpaceService.getCredit(spaceId);
        return creditInfo.canConsume(creditTrainingConsumed);
    }

    /**
     * check train credit.
     *
     * @param spaceId space id
     * @param aiId    ai id
     */
    private void checkTrainCredit(String spaceId, String aiId, AiTrainingDataSource dataSource) {
        boolean canConsume = trainIntercept(spaceId, aiId, dataSource);
        if (!canConsume) {
            throw new BusinessException(LimitException.OVER_LIMIT);
        }
    }

    @Override
    public void createQueryTransaction(String aiId, Training training, Long byWho) {
        AiEntity aiEntity = getByAiId(aiId);
        if (aiEntity == null) {
            throw new BusinessException(AiException.AI_NOT_FOUND);
        }
        // only qa type bot can create query transaction
        if (!aiEntity.getType().equals(AiType.QA.getValue())) {
            return;
        }
        if (training.getId() == null) {
            throw new RuntimeException(
                "credit transaction: training id can not be null: " + training);
        }
        AiCreditTransactionEntity transactionEntity =
            iAiCreditTransactionService.getTransactionByAiIdAndTrainingId(aiId, training.getId());
        if (transactionEntity != null) {
            log.error(
                String.format(
                    "credit transaction: repeat create transaction, ai id: %s, training id: %s",
                    aiId,
                    training.getId()));
            return;
        }
        AiModel aiModel = AiModel.of(aiEntity.getModel());
        EmbeddingText embeddingText =
            new EmbeddingText(aiModel, training.trainingChars(), training.trainingTokens());
        BigDecimal creditConsumed =
            CreditConverter.creditConsumedWithTraining(embeddingText);
        AiCreditTransactionEntity entity = new AiCreditTransactionEntity();
        entity.setSpaceId(aiEntity.getSpaceId());
        entity.setAiId(aiId);
        entity.setTrainingId(training.getId());
        entity.setTransactionType(TransactionType.TRAINING.getValue());
        entity.setAmount(creditConsumed);
        entity.setUserId(byWho);
        entity.setCreatedAt(training.getCompletedAtMillis() != null
            ? ClockManager.me().convertMillis(training.getCompletedAtMillis())
            : ClockManager.me().getLocalDateTimeNow());
        iAiCreditTransactionService.save(entity);
    }
}
