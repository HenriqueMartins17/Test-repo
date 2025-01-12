package com.apitable.appdata.shared.template.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.template.pojo.TemplateProperty;
import org.apache.ibatis.annotations.Param;

public interface TemplatePropertyMapper {

    List<TemplateProperty> selectAllTemplateProperty();

    void insertBatch(@Param("userId") Long userId, @Param("entities") Collection<TemplateProperty> entities);

    void delete();
}
