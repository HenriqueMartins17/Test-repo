package com.apitable.appdata.shared.organization.service.impl;

import com.apitable.appdata.shared.organization.enums.UnitType;
import com.apitable.appdata.shared.organization.mapper.TeamMapper;
import com.apitable.appdata.shared.organization.pojo.Team;
import com.apitable.appdata.shared.organization.service.ITeamService;
import com.apitable.appdata.shared.organization.service.IUnitService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl implements ITeamService {

    @Resource
    private IUnitService iUnitService;

    @Resource
    private TeamMapper teamMapper;

    @Override
    public Long createRootTeam(String spaceId, String spaceName) {
        Team team = new Team();
        team.setId(IdWorker.getId());
        team.setSpaceId(spaceId);
        team.setParentId(0L);
        team.setTeamName(spaceName);
        teamMapper.insert(team);

        iUnitService.create(spaceId, UnitType.TEAM, team.getId());
        return team.getId();
    }

    @Override
    public void deleteBySpaceId(String spaceId) {
        teamMapper.deleteBySpaceId(spaceId);
    }
}
