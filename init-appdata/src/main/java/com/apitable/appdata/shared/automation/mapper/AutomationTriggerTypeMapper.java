package com.apitable.appdata.shared.automation.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.automation.pojo.AutomationTriggerType;
import org.apache.ibatis.annotations.Param;

public interface AutomationTriggerTypeMapper {

    void insertBatch(@Param("entities") Collection<AutomationTriggerType> entities);

    int remove(@Param("userId") Long userId);

    int deleteByTriggerTypeIdIn(@Param("triggerTypeIds") Collection<String> triggerTypeIds);
}
