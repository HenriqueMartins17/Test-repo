/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.social.mapper;

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialWecomPermitDelayEntity;
import com.apitable.enterprise.social.enums.SocialCpIsvPermitDelayProcessStatus;
import com.apitable.enterprise.social.enums.SocialCpIsvPermitDelayType;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * WeCom service provider interface permission delay task processing information
 * </p>
 */
class SocialWecomPermitDelayMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private SocialWecomPermitDelayMapper socialWecomPermitDelayMapper;

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-delay-data.sql")
    void selectByProcessStatusesTest() {
        List<SocialWecomPermitDelayEntity> delayEntities =
            socialWecomPermitDelayMapper.selectByProcessStatuses("wwxxx123", "wwcorpx123123",
                SocialCpIsvPermitDelayType.NOTIFY_BEFORE_TRIAL_EXPIRED.getValue(),
                Collections.singletonList(SocialCpIsvPermitDelayProcessStatus.PENDING.getValue()));
        Assertions.assertTrue(CollUtil.isNotEmpty(delayEntities));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-delay-data.sql")
    void selectBySuiteIdAndProcessStatusTest() {
        List<SocialWecomPermitDelayEntity> delayEntities =
            socialWecomPermitDelayMapper.selectBySuiteIdAndProcessStatus("wwxxx123",
                SocialCpIsvPermitDelayProcessStatus.PENDING.getValue(), 1, 1);
        Assertions.assertTrue(CollUtil.isNotEmpty(delayEntities));
    }

}
