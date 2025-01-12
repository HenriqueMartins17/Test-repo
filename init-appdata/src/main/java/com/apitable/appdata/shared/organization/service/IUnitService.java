package com.apitable.appdata.shared.organization.service;

import com.apitable.appdata.shared.organization.enums.UnitType;

public interface IUnitService {

    void create(String spaceId, UnitType unitType, Long unitRefId);
}
