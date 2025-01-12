package com.apitable.appdata.shared.automation.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.automation.pojo.AutomationService;
import org.apache.ibatis.annotations.Param;

public interface AutomationServiceMapper {

    void insertBatch(@Param("entities") Collection<AutomationService> entities);

    int remove(@Param("userId") Long userId);

    int deleteBySlugIn(@Param("slugs") Collection<String> slugs);
}
