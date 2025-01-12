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

import static org.assertj.core.api.Assertions.assertThat;

import cn.hutool.core.collection.CollUtil;
import com.apitable.AbstractMyBatisMapperTest;
import com.apitable.enterprise.social.entity.SocialTenantDomainEntity;
import com.apitable.enterprise.social.model.SpaceBindDomainDTO;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * <p>
 * Test access layer test: third-party platform integration
 * enterprise tenant exclusive domain name table test
 * </p>
 */
public class SocialTenantDomainMapperTest extends AbstractMyBatisMapperTest {

    @Autowired
    SocialTenantDomainMapper socialTenantDomainMapper;

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testSelectBySpaceId() {
        SocialTenantDomainEntity entity = socialTenantDomainMapper.selectBySpaceId("spc41");
        assertThat(entity).isNotNull();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testSelectBySpaceIds() {
        List<SocialTenantDomainEntity> entities =
            socialTenantDomainMapper.selectBySpaceIds(CollUtil.newArrayList("spc41"));
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testCountTenantDomainName() {
        int count = socialTenantDomainMapper.countTenantDomainName("spc41.com.test");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testSelectSpaceIdByDomainName() {
        String spaceId =
            socialTenantDomainMapper.selectSpaceIdByDomainName("spc41.com.test.vika.ltd");
        assertThat(spaceId).isEqualTo("spc41");
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testSelectSpaceDomainBySpaceIds() {
        List<SpaceBindDomainDTO> entities =
            socialTenantDomainMapper.selectSpaceDomainBySpaceIds(CollUtil.newArrayList("spc41"));
        assertThat(entities).isNotEmpty();
    }

    @Test
    @Sql("/enterprise/sql/social-tenant-domain-data.sql")
    void testSelectSpaceDomainByDomainName() {
        SpaceBindDomainDTO entity =
            socialTenantDomainMapper.selectSpaceDomainByDomainName("spc41.com.test.vika.ltd");
        assertThat(entity).isNotNull();
    }

}
