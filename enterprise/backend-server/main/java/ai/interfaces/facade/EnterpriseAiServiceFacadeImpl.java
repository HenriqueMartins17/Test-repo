package com.apitable.enterprise.ai.interfaces.facade;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.enterprise.ai.entity.AiEntity;
import com.apitable.enterprise.ai.exception.AiException;
import com.apitable.enterprise.ai.model.AiObject;
import com.apitable.enterprise.ai.model.PureJson;
import com.apitable.enterprise.ai.server.Inference;
import com.apitable.enterprise.ai.service.IAiCreditTransactionOverallService;
import com.apitable.enterprise.ai.service.IAiCreditTransactionService;
import com.apitable.enterprise.ai.service.IAiService;
import com.apitable.interfaces.ai.facade.AiServiceFacade;
import com.apitable.interfaces.ai.model.AiCreateParam;
import com.apitable.interfaces.ai.model.AiUpdateParam;
import com.apitable.interfaces.ai.model.ChartTimeDimension;
import com.apitable.interfaces.ai.model.CreditTransactionChartData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * enterprise ai service facade.
 *
 * @author Shawn Deng
 */
public class EnterpriseAiServiceFacadeImpl implements AiServiceFacade {

    private final IAiService aiService;
    private final IAiCreditTransactionService aiCreditTransactionService;
    private final IAiCreditTransactionOverallService aiCreditTransactionOverallService;

    /**
     * constructor.
     *
     * @param aiService                         ai service
     * @param aiCreditTransactionService        ai credit transaction service
     * @param aiCreditTransactionOverallService ai credit transaction overall service
     */
    public EnterpriseAiServiceFacadeImpl(IAiService aiService,
                                         IAiCreditTransactionService aiCreditTransactionService,
                                         IAiCreditTransactionOverallService aiCreditTransactionOverallService) {
        this.aiService = aiService;
        this.aiCreditTransactionService = aiCreditTransactionService;
        this.aiCreditTransactionOverallService = aiCreditTransactionOverallService;
    }

    @Override
    public void createAi(AiCreateParam param) {
        AiObject aiObject = new AiObject();
        aiObject.setSpaceId(param.getSpaceId());
        aiObject.setAiId(param.getAiId());
        aiObject.setName(param.getAiName());
        aiService.create(aiObject);
    }

    @Override
    public void updateAi(String aiId, AiUpdateParam updateParam) {
        AiEntity aiEntity = aiService.getByAiId(aiId);
        AiEntity updateEntity = new AiEntity();
        boolean updateAi = false;
        if (StringUtils.hasText(updateParam.getName())) {
            updateEntity.setName(updateParam.getName());
            updateAi = true;
        }
        if (MapUtil.isNotEmpty(updateParam.getSetting())) {
            JSONObject settingJsonObj = JSONUtil.parseObj(updateParam.getSetting());
            if (settingJsonObj.containsKey("type") &&
                StrUtil.isNotBlank(settingJsonObj.getStr("type"))) {
                String type = settingJsonObj.getStr("type");
                updateEntity.setType(type);
                if (StrUtil.isBlank(settingJsonObj.getStr("model"))) {
                    // get setting from schema
                    PureJson aiSettingSchema = Inference.getAiSetting(aiId, type);
                    if (aiSettingSchema == null) {
                        throw new BusinessException(AiException.AI_SETTING_NOT_SET);
                    }
                    Map<String, Object> data = aiSettingSchema.extractData();
                    updateEntity.setModel(MapUtil.getStr(data, "model"));
                }
            }
            if (settingJsonObj.containsKey("model") &&
                StrUtil.isNotBlank(settingJsonObj.getStr("model"))) {
                updateEntity.setModel(settingJsonObj.getStr("model"));
            }
            updateEntity.setSetting(settingJsonObj.toJSONString(0));
            updateAi = true;
        }
        if (updateAi) {
            updateEntity.setId(aiEntity.getId());
            aiService.updateById(updateEntity);
        }
    }

    @Override
    public void deleteAi(List<String> aiIds) {
        aiService.deleteAi(aiIds);
    }

    @Override
    public BigDecimal getUsedCreditCount(String spaceId, LocalDate beginDate, LocalDate endDate) {
        return aiCreditTransactionService.countDateRangeAmount(spaceId, beginDate,
            endDate);
    }

    @Override
    public List<CreditTransactionChartData> loadCreditTransactionChartData(
        String spaceId, ChartTimeDimension chartTimeDimension) {
        return aiCreditTransactionOverallService.summary(spaceId, chartTimeDimension);
    }
}
