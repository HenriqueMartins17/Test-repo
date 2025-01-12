package com.apitable.enterprise.infoflow.service;

import java.util.List;

public interface IUnitThirdPartService {

    /**
     * Get recent User form infoflow.
     * @param userId
     * @return List<String> uid
     */
   List<String> getUserRecent(Long userId);

    /**
     * Get UnitIds by UnionIds And SpaceId , order by unionIds.
     * @param spaceId
     * @param unionIds
     * @return unitIds
     */
    List<Long> getUnitIdsByUnionIdsAndSpaceId(String spaceId, List<String> unionIds);
}
