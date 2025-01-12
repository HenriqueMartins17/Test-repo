package com.apitable.appdata.shared.organization.service.impl;

import com.apitable.appdata.shared.organization.enums.UnitType;
import com.apitable.appdata.shared.organization.mapper.MemberMapper;
import com.apitable.appdata.shared.organization.pojo.Member;
import com.apitable.appdata.shared.organization.service.IMemberService;
import com.apitable.appdata.shared.organization.service.IUnitService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements IMemberService {

    @Resource
    private IUnitService iUnitService;

    @Resource
    private MemberMapper memberMapper;

    @Override
    public Long create(Long userId, String spaceId, String name, String email) {
        Member member = new Member();
        member.setId(IdWorker.getId());
        member.setUserId(userId);
        member.setSpaceId(spaceId);
        member.setMemberName(name);
        member.setEmail(email);
        member.setStatus(0);
        member.setIsAdmin(true);
        member.setIsActive(true);
        memberMapper.insert(member);

        iUnitService.create(spaceId, UnitType.MEMBER, member.getId());
        return member.getId();
    }

    @Override
    public void deleteBySpaceId(String spaceId) {
        memberMapper.deleteBySpaceId(spaceId);
    }
}
