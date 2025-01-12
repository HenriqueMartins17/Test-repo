package com.apitable.appdata.shared.template.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.template.pojo.TemplatePropertyRel;
import org.apache.ibatis.annotations.Param;

public interface TemplatePropertyRelMapper {

    int insertBatch(@Param("entities") Collection<TemplatePropertyRel> entities);

    void delete();
}
