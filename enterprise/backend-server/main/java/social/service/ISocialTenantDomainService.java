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

package com.apitable.enterprise.social.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.social.model.SpaceBindDomainDTO;
import com.apitable.enterprise.social.entity.SocialTenantDomainEntity;

/**
 * <p>
 * Third party platform integration - enterprise tenant exclusive domain name service interface
 * </p>
 */
public interface ISocialTenantDomainService extends IService<SocialTenantDomainEntity> {

    /**
     * Create domain name (status: binding)
     *
     * @param spaceId       Space ID
     * @param domainPrefix  Domain name prefix
     * @param domainName    DOMAIN NAME
     */
    void createDomain(String spaceId, String domainPrefix, String domainName);

    /**
     * Enable domain name
     *
     * @param spaceId   Space ID
     */
    void enabledDomain(String spaceId);

    /**
     * Delete the domain name and delete the domain name DDNS resolution record
     *
     * @param spaceIds Space ID
     */
    void removeDomain(List<String> spaceIds);

    /**
     * Get the space station domain name
     *
     * @param spaceId           Space ID
     * @param appendHttpsPrefix  Whether to add https prefix automatically. If false, the domain name without protocol will be returned
     * @return Domain name (return to the public domain name if it is not available)
     */
    String getDomainNameBySpaceId(String spaceId, boolean appendHttpsPrefix);

    /**
     * Obtain the domain name of the space station (domain name without http protocol header)
     *
     * @param spaceId           Space ID
     * @return Domain name (return to the public domain name if it is not available)
     */
    default String getDomainNameBySpaceId(String spaceId) {
        return getDomainNameBySpaceId(spaceId, false);
    }

    /**
     * Get the default domain name of the space from the configuration file
     *
     * @return primary domain name
     */
    String getSpaceDefaultDomainName();

    /**
     * Get Space ID
     *
     * @param domainName    Enterprise domain name
     * @return spaceId
     */
    String getSpaceIdByDomainName(String domainName);

    /**
     * Batch acquisition of space station domain names
     *
     * @param spaceIds Space Ids
     * @return Domain name corresponding to the space station (unfiltered)
     */
    List<SpaceBindDomainDTO> getSpaceDomainBySpaceIds(List<String> spaceIds);

    /**
     * Batch acquisition of space station domain names
     *
     * @param spaceIds   Space Ids
     * @return key:Space ID，value:Space station binding domain name
     */
    Map<String, String> getSpaceDomainBySpaceIdsToMap(List<String> spaceIds);

    /**
     * Get the domain name binding space status
     *
     * @param domainName Domain name
     * @return Domain name binding information
     */
    SpaceBindDomainDTO getSpaceDomainByDomainName(String domainName);

}
