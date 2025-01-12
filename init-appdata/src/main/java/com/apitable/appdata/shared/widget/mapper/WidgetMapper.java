package com.apitable.appdata.shared.widget.mapper;

import java.util.Collection;
import java.util.List;

import com.apitable.appdata.shared.widget.pojo.Widget;
import org.apache.ibatis.annotations.Param;

public interface WidgetMapper {

    List<Widget> selectByNodeIds(@Param("nodeIds") Collection<String> nodeIds);

    int insertBatch(@Param("userId") Long userId, @Param("entities") List<Widget> entities);
}
