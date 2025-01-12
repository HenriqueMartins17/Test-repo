package com.apitable.appdata.shared.widget.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.widget.pojo.WidgetPackage;
import org.apache.ibatis.annotations.Param;

public interface WidgetPackageMapper {

    List<WidgetPackage> selectByPackageIds(@Param("packageIds") Collection<String> packageIds);

    int insertBatch(@Param("entities") List<WidgetPackage> entities);

    int disableGlobalWidgetPackage(@Param("userId") Long userId);
}
