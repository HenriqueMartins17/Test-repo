package com.apitable.appdata.shared.organization.service.impl;

import cn.hutool.core.util.IdUtil;
import com.apitable.appdata.shared.organization.enums.UnitType;
import com.apitable.appdata.shared.organization.mapper.UnitMapper;
import com.apitable.appdata.shared.organization.pojo.Unit;
import com.apitable.appdata.shared.organization.service.IUnitService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UnitServiceImpl implements IUnitService {

    @Resource
    private UnitMapper unitMapper;

    @Override
    public void create(String spaceId, UnitType unitType, Long unitRefId) {
        Unit unit = new Unit();
        unit.setId(IdWorker.getId());
        unit.setUnitId(IdUtil.fastSimpleUUID());
        unit.setSpaceId(spaceId);
        unit.setUnitType(unitType.getType());
        unit.setUnitRefId(unitRefId);
        unitMapper.insert(unit);
    }
}
