package com.apitable.appdata.shared.organization.service.impl;

import com.apitable.appdata.shared.organization.mapper.TeamMemberRelMapper;
import com.apitable.appdata.shared.organization.pojo.TeamMemberRel;
import com.apitable.appdata.shared.organization.service.ITeamMemberRelService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberRelServiceImpl implements ITeamMemberRelService {

    @Resource
    private TeamMemberRelMapper teamMemberRelMapper;

    @Override
    public void create(Long teamId, Long memberId) {
        TeamMemberRel teamMemberRel = new TeamMemberRel();
        teamMemberRel.setId(IdWorker.getId());
        teamMemberRel.setTeamId(teamId);
        teamMemberRel.setMemberId(memberId);
        teamMemberRelMapper.insert(teamMemberRel);
    }
}
