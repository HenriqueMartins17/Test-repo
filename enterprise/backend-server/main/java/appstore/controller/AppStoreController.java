/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up
 * license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its
 * subdirectories does not constitute permission to use this code or APITable Enterprise Edition
 * features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.appstore.controller;


import cn.hutool.core.util.StrUtil;
import com.apitable.core.support.ResponseData;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.model.AppInfo;
import com.apitable.enterprise.appstore.setting.AppStore;
import com.apitable.enterprise.appstore.setting.AppStoreConfig;
import com.apitable.enterprise.appstore.setting.AppStoreConfigLoader;
import com.apitable.enterprise.social.service.IFeishuService;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.shared.util.page.PageHelper;
import com.apitable.shared.util.page.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Application store library interface.
 */
@RestController
@Tag(name = "App Store")
@ApiResource(path = "/appstores")
public class AppStoreController {

    @Resource
    private IFeishuService iFeishuService;

    /**
     * Query application list.
     */
    @GetResource(path = "/apps", requiredPermission = false)
    @Operation(summary = "Query application list", description = "Pagination query. If no query "
        + "parameter is transferred, the default query will be used")
    @Parameters({
        @Parameter(name = "pageIndex", description = "Page Index", schema = @Schema(type =
            "string"), in = ParameterIn.QUERY, example = "1"),
        @Parameter(name = "pageSize", description = "Quantity per page", schema = @Schema(type =
            "string"), in = ParameterIn.QUERY, example = "50"),
        @Parameter(name = "orderBy", description = "Sort field", schema =
        @Schema(type = "string"), in = ParameterIn.QUERY, example = "createdAt"),
        @Parameter(name = "sortBy", description = "Collation,asc=positive sequence,desc=reverse "
            + "order", schema = @Schema(type = "string"), in = ParameterIn.QUERY, example = "desc"),
    })
    public ResponseData<PageInfo<AppInfo>> fetchAppStoreApps(
        @RequestParam(name = "pageIndex", required = false, defaultValue = "1") Integer pageIndex,
        @RequestParam(name = "pageSize", required = false, defaultValue = "50") Integer pageSize) {
        AppStoreConfig appStoreConfig = AppStoreConfigLoader.getConfig();
        List<AppInfo> appInfoList = new ArrayList<>(appStoreConfig.size());
        appStoreConfig.entrySet().stream()
            .sorted(Comparator.comparing(o -> o.getValue().getDisplayOrder()))
            .forEachOrdered(entry -> {
                AppStore appStore = entry.getValue();
                AppInfo appInfo = new AppInfo();
                appInfo.setAppId(entry.getKey());
                appInfo.setName(I18nStringsUtil.t(appStore.getAppName()));
                appInfo.setType(appStore.getType());
                appInfo.setAppType(appStore.getAppType());
                appInfo.setStatus(appStore.getStatus());
                appInfo.setIntro(I18nStringsUtil.t(appStore.getIntro()));
                appInfo.setHelpUrl(appStore.getHelpUrl());
                appInfo.setDescription(I18nStringsUtil.t(appStore.getDescription()));
                appInfo.setDisplayImages(
                    Collections.singletonList(appStore.getInlineImage().getUrl()));
                appInfo.setNotice(I18nStringsUtil.t(appStore.getNotice()));
                appInfo.setLogoUrl(appStore.getLogo().getUrl());
                appInfo.setNeedConfigured(appStore.isNeedConfigured());
                appInfo.setConfigureUrl(appStore.getConfigureUrl());
                appInfo.setNeedAuthorize(appStore.isNeedAuthorize());
                AppType appType = AppType.of(appStore.getType());
                if (appType == AppType.LARK_STORE && StrUtil.isNotBlank(
                    appStore.getStopActionUrl())) {
                    String isvAppId = iFeishuService.getIsvAppId();
                    if (StrUtil.isNotBlank(isvAppId)) {
                        appInfo.setStopActionUrl(
                            StrUtil.format(appStore.getStopActionUrl(), isvAppId));
                    }
                }
                appInfoList.add(appInfo);
            });
        return ResponseData.success(
            PageHelper.build(pageIndex, pageSize, appStoreConfig.size(), appInfoList));
    }
}
