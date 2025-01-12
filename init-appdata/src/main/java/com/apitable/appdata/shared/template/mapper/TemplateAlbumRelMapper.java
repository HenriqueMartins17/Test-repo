package com.apitable.appdata.shared.template.mapper;

import java.util.List;

import com.apitable.appdata.shared.template.pojo.TemplateAlbumRel;
import org.apache.ibatis.annotations.Param;

public interface TemplateAlbumRelMapper {

    int insertBatch(@Param("entities") List<TemplateAlbumRel> entities);

    void delete();
}
