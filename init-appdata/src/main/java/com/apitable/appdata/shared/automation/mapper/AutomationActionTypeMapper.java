package com.apitable.appdata.shared.automation.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.automation.pojo.AutomationActionType;
import org.apache.ibatis.annotations.Param;

public interface AutomationActionTypeMapper {

    void insertBatch(@Param("entities") Collection<AutomationActionType> entities);

    int remove(@Param("userId") Long userId);

    int deleteByActionTypeIdIn(@Param("actionTypeIds") Collection<String> actionTypeIds);
}
