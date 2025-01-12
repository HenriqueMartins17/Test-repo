package com.apitable.enterprise.gm.service.impl;


import static org.assertj.core.api.Assertions.assertThat;

import com.apitable.enterprise.AbstractVikaSaasIntegrationTest;
import com.apitable.internal.ro.SpaceStatisticsRo;
import com.apitable.internal.service.InternalSpaceService;
import com.apitable.mock.bean.MockUserSpace;
import com.apitable.space.dto.DatasheetStaticsDTO;
import com.apitable.space.service.IStaticsService;
import com.apitable.workspace.enums.ViewType;
import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;

public class InternalSpaceServiceImplTest extends AbstractVikaSaasIntegrationTest {

    @Resource
    private InternalSpaceService internalSpaceService;

    @Resource
    private IStaticsService iStaticsService;

    @Test
    public void updateSpaceStatisticsInCacheDataIsNull() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iStaticsService.setDatasheetStaticsBySpaceIdToCache(userSpace.getSpaceId(),
            new DatasheetStaticsDTO());
        internalSpaceService.updateSpaceStatisticsInCache(userSpace.getSpaceId(), null);
        DatasheetStaticsDTO result =
            iStaticsService.getDatasheetStaticsBySpaceId(userSpace.getSpaceId());
        assertThat(result.getGanttViews()).isEqualTo(0L);
    }

    @Test
    public void updateSpaceStatisticsInCacheDataViewCountIsNull() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iStaticsService.setDatasheetStaticsBySpaceIdToCache(userSpace.getSpaceId(),
            new DatasheetStaticsDTO());
        SpaceStatisticsRo ro = new SpaceStatisticsRo();
        internalSpaceService.updateSpaceStatisticsInCache(userSpace.getSpaceId(), ro);
        DatasheetStaticsDTO result =
            iStaticsService.getDatasheetStaticsBySpaceId(userSpace.getSpaceId());
        assertThat(result.getGanttViews()).isEqualTo(0L);
    }

    @Test
    public void updateSpaceStatisticsInCacheDataViewCount() {
        MockUserSpace userSpace = createSingleUserAndSpace();
        iStaticsService.setDatasheetStaticsBySpaceIdToCache(userSpace.getSpaceId(),
            new DatasheetStaticsDTO());
        SpaceStatisticsRo ro = new SpaceStatisticsRo();
        Map<Integer, Long> viewCount = new HashMap<>();
        viewCount.put(ViewType.GANTT.getType(), 1L);
        ro.setViewCount(viewCount);
        internalSpaceService.updateSpaceStatisticsInCache(userSpace.getSpaceId(), ro);
        DatasheetStaticsDTO result =
            iStaticsService.getDatasheetStaticsBySpaceId(userSpace.getSpaceId());
        assertThat(result.getGanttViews()).isEqualTo(1L);
    }
}
