package com.apitable.appdata.shared.automation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import com.apitable.appdata.shared.automation.pojo.AutomationActionType;
import com.apitable.appdata.shared.automation.pojo.AutomationService;
import com.apitable.appdata.shared.automation.pojo.AutomationTriggerType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AutomationDataPack {

    private List<AutomationService> services;

    @JsonProperty("trigger_types")
    private List<AutomationTriggerType> triggerTypes;

    @JsonProperty("action_types")
    private List<AutomationActionType> actionTypes;

}
