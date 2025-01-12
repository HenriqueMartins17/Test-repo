/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.automation.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.automation.entity.AutomationActionEntity;
import com.apitable.automation.entity.AutomationRobotEntity;
import com.apitable.automation.entity.AutomationTriggerEntity;
import com.apitable.automation.mapper.AutomationRobotMapper;
import com.apitable.automation.model.RobotTriggerDto;
import com.apitable.automation.service.IAutomationActionService;
import com.apitable.automation.service.IAutomationActionTypeService;
import com.apitable.automation.service.IAutomationRobotService;
import com.apitable.automation.service.IAutomationTriggerService;
import com.apitable.automation.service.IAutomationTriggerTypeService;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.automation.model.AutomationApiTriggerCreateRo;
import com.apitable.enterprise.automation.model.AutomationTriggerCreateVo;
import com.apitable.enterprise.automation.service.IAutomationOpenService;
import com.apitable.shared.util.IdUtil;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AutomationOpenServiceImpl implements IAutomationOpenService {

    @Resource
    private IAutomationRobotService iAutomationRobotService;

    @Resource
    private AutomationRobotMapper automationRobotMapper;

    @Resource
    private IAutomationTriggerService iAutomationTriggerService;

    @Resource
    private IAutomationTriggerTypeService iAutomationTriggerTypeService;

    @Resource
    private IAutomationActionService iAutomationActionService;

    @Resource
    private IAutomationActionTypeService iAutomationActionTypeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseData<AutomationTriggerCreateVo> upsert(AutomationApiTriggerCreateRo data,
                                                          String xServiceToken) {
        AutomationTriggerCreateVo automationTriggerCreateVo = new AutomationTriggerCreateVo();

        // 1、Creating a robot
        String robotId = IdUtil.createAutomationRobotId();
        AutomationRobotEntity robot = AutomationRobotEntity.builder()
            .name(data.getRobot().getName())
            .description(data.getRobot().getDescription())
            .robotId(robotId)
            .resourceId(data.getRobot().getResourceId())
            .seqId(data.getSeqId())
            .xServiceToken(xServiceToken)
            .isActive(true)
            .build();
        String[] typeNameId = StrUtil.splitToArray(data.getTrigger().getTypeName(), "@");
        String triggerTypeId =
            iAutomationTriggerTypeService.getTriggerTypeByEndpoint(typeNameId[0]);
        AutomationTriggerEntity trigger = AutomationTriggerEntity.builder()
            .robotId(robotId)
            .triggerTypeId(triggerTypeId)
            .input(this.transformInput(data.getTrigger().getInput()))
            .build();
        try {
            iAutomationRobotService.create(robot);
        } catch (Exception e) {
            // When an insert is performed, adjust to update form.
            List<RobotTriggerDto> robotTriggerDtoList =
                automationRobotMapper.getRobotTriggers(data.getSeqId(),
                    data.getRobot().getResourceId());
            if (robotTriggerDtoList.size() > 0) {
                // Update existing robots.
                robotTriggerDtoList.forEach(i -> {
                    automationTriggerCreateVo.setTriggerId(i.getTriggerId());
                    automationTriggerCreateVo.setRobotId(i.getRobotId());
                });
                robot.setRobotId(automationTriggerCreateVo.getRobotId());
                iAutomationRobotService.updateByRobotId(robot);
                trigger.setRobotId(automationTriggerCreateVo.getRobotId());
                trigger.setTriggerId(automationTriggerCreateVo.getTriggerId());
                iAutomationTriggerService.updateByTriggerId(trigger);
                this.updateRequestAction(automationTriggerCreateVo.getRobotId(),
                    automationTriggerCreateVo.getTriggerId(), "POST", data.getWebhookUrl());
                return ResponseData.success(automationTriggerCreateVo);
            }
            log.info("create robot fail , err:{}", e.getMessage());
            return ResponseData.status(false, 602, "Do not repeat the creation.")
                .data(automationTriggerCreateVo);
        }

        // 2、Creating a trigger
        String triggerId = IdUtil.createAutomationTriggerId();
        trigger.setTriggerId(triggerId);
        iAutomationTriggerService.create(trigger);

        // 3、Create an action to perform the action
        this.createRequestAction(robotId, triggerId, "POST", data.getWebhookUrl());
        automationTriggerCreateVo.setRobotId(robotId);
        automationTriggerCreateVo.setTriggerId(triggerId);
        return ResponseData.success(automationTriggerCreateVo);
    }

    /**
     * Convert content to the trigger format.
     *
     * @param input input data
     * @return json object
     */
    private String transformInput(JSONObject input) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOnce("type", "Expression");
        JSONObject jsonValue = new JSONObject();
        JSONArray operands = new JSONArray();
        input.forEach((k, v) -> {
            operands.put(k);
            operands.put(v);
        });
        jsonValue.putOnce("operands", operands);
        jsonValue.putOnce("operator", "newObject");
        jsonObject.putOnce("value", jsonValue);
        return StrUtil.toString(jsonObject);
    }

    private void createRequestAction(String robotId, String triggerId, String method,
                                     String webhookUrl) {
        String actionId = IdUtil.createAutomationActionId();
        String input = this.buildAutomationApiAction(triggerId, method, webhookUrl);

        String actionTypeId = iAutomationActionTypeService.getActionTypeIdByEndpoint("sendRequest");
        AutomationActionEntity action = AutomationActionEntity.builder()
            .actionId(actionId)
            .robotId(robotId)
            .actionTypeId(actionTypeId)
            .input(input)
            .build();
        iAutomationActionService.create(action);
    }

    private void updateRequestAction(String robotId, String triggerId, String method,
                                     String webhookUrl) {
        String input = this.buildAutomationApiAction(triggerId, method, webhookUrl);
        String actionTypeId = iAutomationActionTypeService.getActionTypeIdByEndpoint("sendRequest");
        iAutomationActionService.updateActionTypeIdAndInputByRobotId(robotId, actionTypeId, input);
    }

    /**
     * Create the action request body.
     */
    private String buildAutomationApiAction(String triggerId, String method, String webhookUrl) {
        JSONObject actionObject = new JSONObject();
        actionObject.putOnce("type", "Expression");
        JSONObject valueObject = new JSONObject();
        JSONObject dataObject = new JSONObject();
        dataObject.putAll(new HashMap<String, Object>() {
            {
                put("type", "Expression");
                put("value", new JSONObject() {
                    {
                        putOnce("operator", "JSONStringify")
                            .putOnce("operands", new ArrayList<Object>() {{
                                add(new HashMap<String, Object>() {{
                                    put("type", "Expression");
                                    put("value", new HashMap<String, Object>() {{
                                        put("operands", new ArrayList<Map<String, Object>>() {
                                            {
                                                add(new HashMap<String, Object>() {{
                                                    put("type", "Literal");
                                                    put("value", triggerId);
                                                }});
                                            }
                                        });
                                        put("operator", "getNodeOutput");
                                    }});
                                }});
                            }});
                    }
                });
            }
        });
        valueObject.putOnce("operator", "newObject");
        valueObject.putOnce("operands", new JSONArray().put("body")
            .put(new JSONObject().
                putOnce("type", "Expression").
                putOnce("value", new JSONObject().
                    putOnce("operator", "newObject").
                    putOnce("operands", new JSONArray()
                        .put("type")
                        .put(new JSONObject().putOnce("type", "Literal").putOnce("value", "json"))
                        .put("data")
                        .put(dataObject)
                    )))
            .put("method")
            .put(new JSONObject().putOnce("type", "Literal").putOnce("value", method))
            .put("url")
            .put(new JSONObject().putOnce("type", "Literal").putOnce("value", webhookUrl))
        );
        actionObject.putOnce("value", valueObject);
        return JSONUtil.toJsonStr(actionObject);
    }

}
