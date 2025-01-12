package com.apitable.appdata.shared.widget.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.widget.pojo.WidgetPackageRelease;
import org.apache.ibatis.annotations.Param;

public interface WidgetPackageReleaseMapper {

    List<WidgetPackageRelease> selectByIds(@Param("ids") Collection<Long> ids);

    int insertBatch(@Param("entities") List<WidgetPackageRelease> entities);
}
