package com.apitable.appdata.shared.template.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.template.pojo.Template;
import org.apache.ibatis.annotations.Param;

public interface TemplateMapper {

    List<Template> selectByTypeId(@Param("typeId") String typeId);

    List<String> selectTemplateIdByTemplateIds(@Param("templateIds") Collection<String> templateIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<Template> entities);

    void remove(@Param("userId") Long userId, @Param("templateIds") Collection<String> templateIds);

    void delete(@Param("templateIds") Collection<String> templateIds);
}
