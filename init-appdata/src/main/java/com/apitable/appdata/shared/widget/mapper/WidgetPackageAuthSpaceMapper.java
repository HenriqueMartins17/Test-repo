package com.apitable.appdata.shared.widget.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.widget.pojo.WidgetPackageAuthSpace;
import org.apache.ibatis.annotations.Param;

public interface WidgetPackageAuthSpaceMapper {

    List<WidgetPackageAuthSpace> selectByPackageIds(@Param("packageIds") Collection<String> packageIds);

    int insertBatch(@Param("entities") List<WidgetPackageAuthSpace> entities);
}
