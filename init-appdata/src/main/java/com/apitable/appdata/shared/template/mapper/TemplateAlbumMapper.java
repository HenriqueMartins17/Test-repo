package com.apitable.appdata.shared.template.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.template.pojo.TemplateAlbum;
import org.apache.ibatis.annotations.Param;

public interface TemplateAlbumMapper {

    List<TemplateAlbum> selectAllTemplateAlbum();

    void insertBatch(@Param("userId") Long userId, @Param("entities") Collection<TemplateAlbum> entities);

    void delete();
}
