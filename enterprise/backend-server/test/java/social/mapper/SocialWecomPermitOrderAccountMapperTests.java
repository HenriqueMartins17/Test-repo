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
import com.apitable.enterprise.social.entity.SocialWecomPermitOrderAccountEntity;
import com.apitable.enterprise.social.enums.SocialCpIsvPermitActivateStatus;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * WeCom service provider interface license account information
 * </p>
 */
class SocialWecomPermitOrderAccountMapperTests extends AbstractMyBatisMapperTest {

    @Autowired
    private SocialWecomPermitOrderAccountMapper socialWecomPermitOrderAccountMapper;

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void selectByActiveCodesTest() {
        List<SocialWecomPermitOrderAccountEntity> accountEntities =
            socialWecomPermitOrderAccountMapper
                .selectByActiveCodes("wwxxx123", "wwcorpx123123", Arrays.asList("ac1", "ac2"));
        Assertions.assertTrue(CollUtil.isNotEmpty(accountEntities));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void selectByExpireTimeTest() {
        LocalDateTime expireTime = LocalDateTime.of(2023, 7, 29, 19, 14, 9);
        List<SocialWecomPermitOrderAccountEntity> accountEntities =
            socialWecomPermitOrderAccountMapper
                .selectByExpireTime("wwxxx123", "wwcorpx123123", expireTime);
        Assertions.assertTrue(CollUtil.isNotEmpty(accountEntities));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void selectActiveCodesTest() {
        List<String> activeCodes = socialWecomPermitOrderAccountMapper
            .selectActiveCodes("wwxxx123", "wwcorpx123123", null);
        Assertions.assertTrue(CollUtil.isNotEmpty(activeCodes));

        activeCodes = socialWecomPermitOrderAccountMapper
            .selectActiveCodes("wwxxx123", "wwcorpx123123",
                Collections.singletonList(SocialCpIsvPermitActivateStatus.ACTIVATED.getValue()));
        Assertions.assertTrue(CollUtil.isNotEmpty(activeCodes));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void selectActiveCodesByActiveCodesAndStatusTest() {
        List<String> activeCodes = socialWecomPermitOrderAccountMapper
            .selectActiveCodesByActiveCodesAndStatus("wwxxx123", "wwcorpx123123",
                Arrays.asList("ac1", "ac2"), null);
        Assertions.assertTrue(CollUtil.isNotEmpty(activeCodes));

        activeCodes = socialWecomPermitOrderAccountMapper
            .selectActiveCodesByActiveCodesAndStatus("wwxxx123", "wwcorpx123123",
                Arrays.asList("ac1", "ac2"),
                Collections.singletonList(SocialCpIsvPermitActivateStatus.ACTIVATED.getValue()));
        Assertions.assertTrue(CollUtil.isNotEmpty(activeCodes));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void selectCpUserIdsByStatusTest() {
        List<String> cpUserIds =
            socialWecomPermitOrderAccountMapper.selectCpUserIdsByStatus("wwxxx123", "wwcorpx123123",
                Collections.singletonList(SocialCpIsvPermitActivateStatus.ACTIVATED.getValue()));
        Assertions.assertTrue(CollUtil.isNotEmpty(cpUserIds));
    }

    @Test
    @Sql("/enterprise/sql/social-wecom-permit-order-account-data.sql")
    void updateActiveStatusByActiveCodesTest() {
        int result = socialWecomPermitOrderAccountMapper
            .updateActiveStatusByActiveCodes("wwxxx123", "wwcorpx123123",
                Arrays.asList("ac1", "ac2"),
                SocialCpIsvPermitActivateStatus.EXPIRED.getValue());
        Assertions.assertNotEquals(0, result);
    }

}
