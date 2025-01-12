package com.apitable.appdata.shared.player.service.impl;

import com.apitable.appdata.shared.player.mapper.PlayerActivityMapper;
import com.apitable.appdata.shared.player.pojo.PlayerActivity;
import com.apitable.appdata.shared.player.service.IPlayerActivityService;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlayerActivityServiceImpl implements IPlayerActivityService {

    @Resource
    private PlayerActivityMapper playerActivityMapper;

    @Override
    public void create(List<PlayerActivity> playerActivities) {
        playerActivityMapper.insertBatch(playerActivities);
    }
}
