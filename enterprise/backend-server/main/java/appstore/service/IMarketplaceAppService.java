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

package com.apitable.enterprise.appstore.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

import com.apitable.enterprise.appstore.entity.MarketplaceSpaceAppRelEntity;
import com.apitable.enterprise.appstore.model.MarketplaceSpaceAppVo;

/**
 *
 * <p>
 * Marketplace App Service
 * </p>
 */
@Deprecated
public interface IMarketplaceAppService extends IService<MarketplaceSpaceAppRelEntity> {

    /**
     * Query the application list of the space station
     */
    List<MarketplaceSpaceAppVo> getSpaceAppList(String spaceId);

    /**
     * Get the list of application IDs that the space has opened
     * * Old version data will be deleted soon
     */
    List<String> getAppIdsBySpaceId(String spaceId);

    /**
     * Check if space and apps are enabled
     */
    boolean checkBySpaceIdAndAppId(String spaceId, String appId);

    /**
     * Get the application entity that is opened in the specified space
     */
    MarketplaceSpaceAppRelEntity getBySpaceIdAndAppId(String spaceId, String appId);

    /**
     * Remove the open application of the space
     */
    void removeBySpaceIdAndAppId(String spaceId, String appId);

    /**
     * Open the application
     */
    void openSpaceApp(String spaceId, String appId);

    /**
     * Stop the application
     */
    void stopSpaceApp(String spaceId, String appId);
}
