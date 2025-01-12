package com.apitable.appdata.shared.player.mapper;

import java.util.Collection;

import com.apitable.appdata.shared.player.pojo.PlayerActivity;
import org.apache.ibatis.annotations.Param;

public interface PlayerActivityMapper {

    int insertBatch(@Param("entities") Collection<PlayerActivity> entities);
}
