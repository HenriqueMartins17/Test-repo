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

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import com.apitable.enterprise.social.model.SpaceBindDomainDTO;
import com.apitable.enterprise.social.entity.SocialTenantDomainEntity;

/**
 * <p>
 * Third party platform integration - enterprise tenant exclusive domain name Mapper
 * </p>
 */
public interface SocialTenantDomainMapper extends BaseMapper<SocialTenantDomainEntity> {

    /**
     * Query domain name information according to the space
     *
     * @param spaceId   Space Id
     * @return Domain name information
     */
    SocialTenantDomainEntity selectBySpaceId(@Param("spaceId") String spaceId);

    /**
     * Batch query of space domain name information
     *
     * @param spaceIds Space ID Collection
     * @return Domain name information
     */
    List<SocialTenantDomainEntity> selectBySpaceIds(@Param("spaceIds") List<String> spaceIds);

    /**
     * Count the number of duplicate prefixes of enterprise WeChat exclusive domain names
     *
     * @param domainPrefix Prefix domain name (all lowercase space Id)
     * @return int Number of domain names
     */
    int countTenantDomainName(String domainPrefix);

    /**
     * Query space ID
     *
     * @param domainName Enterprise domain name
     * @return Space Id
     */
    String selectSpaceIdByDomainName(@Param("domainName") String domainName);

    /**
     * Query the domain name of the space station
     *
     * @param spaceIds Space Ids
     * @return Domain Name Collection
     */
    List<SpaceBindDomainDTO> selectSpaceDomainBySpaceIds(@Param("spaceIds") List<String> spaceIds);

    /**
     * Modify domain name status
     *
     * @param spaceId Space Id
     * @param domainStatus State
     * @return int
     */
    int updateStatusBySpaceId(@Param("spaceId") String spaceId, @Param("domainStatus") int domainStatus);

    /**
     * Get the domain name binding space status
     *
     * @param domainName Domain name
     * @return Domain name binding information
     */
    SpaceBindDomainDTO selectSpaceDomainByDomainName(@Param("domainName") String domainName);

    /**
     * Delete the space domain name according to the space station ID
     *
     * @param spaceIds Space ID Collection
     * @return int  Number of rows affected
     */
    int deleteSpaceDomainBySpaceIds(@Param("spaceIds") List<String> spaceIds);

}
