package com.apitable.enterprise.automation.interfaces.facade;

import com.apitable.enterprise.automation.service.IAutomationTriggerScheduleService;
import com.apitable.interfaces.automation.facede.AutomationServiceFacade;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * enterprise automation service facade.
 */
public class EnterpriseAutomationServiceFacadeImpl implements AutomationServiceFacade {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(EnterpriseAutomationServiceFacadeImpl.class);

    private final IAutomationTriggerScheduleService iAutomationTriggerScheduleService;

    public EnterpriseAutomationServiceFacadeImpl(
        IAutomationTriggerScheduleService iAutomationTriggerScheduleService) {
        this.iAutomationTriggerScheduleService = iAutomationTriggerScheduleService;
    }


    @Override
    public void publishSchedule(Long scheduleId) {
        try {
            iAutomationTriggerScheduleService.publishTriggerSchedule(scheduleId);
        } catch (Exception e) {
            LOGGER.error("PublishScheduleError: {}", scheduleId, e);
        }

    }

    @Override
    public void copy(Map<String, String> newTriggerMap) {
        iAutomationTriggerScheduleService.copy(newTriggerMap);
    }

    @Override
    public void createSchedule(String spaceId, String triggerId, String scheduleConfig) {
        iAutomationTriggerScheduleService.createTriggerSchedule(spaceId, triggerId, scheduleConfig);
    }

    @Override
    public void updateSchedule(String triggerId, String scheduleConfig) {
        iAutomationTriggerScheduleService.updateScheduleConfig(triggerId, scheduleConfig);
    }

    @Override
    public void deleteSchedule(String triggerId, Long userId) {
        iAutomationTriggerScheduleService.deleteByTriggerId(triggerId, userId);
    }
}
