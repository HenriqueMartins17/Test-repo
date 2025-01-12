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

package com.apitable.enterprise.appstore.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jakarta.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import com.apitable.enterprise.appstore.component.yozo.YozoApiException;
import com.apitable.enterprise.appstore.component.yozo.YozoTemplate;
import com.apitable.asset.mapper.AssetMapper;
import com.apitable.asset.ro.AttachOfficePreviewRo;
import com.apitable.base.enums.ActionException;
import com.apitable.enterprise.appstore.enums.AppType;
import com.apitable.enterprise.appstore.service.IAppInstanceService;
import com.apitable.enterprise.appstore.service.IOfficeService;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import static com.apitable.enterprise.appstore.enums.AppException.APP_NOT_OPEN;

@Service
@Slf4j
public class OfficeServiceImpl implements IOfficeService {

    @Autowired(required = false)
    private YozoTemplate yozoTemplate;

    @Resource
    private IAppInstanceService iAppInstanceService;

    @Resource
    private AssetMapper assetMapper;

    @Resource
    private ConstProperties constProperties;

    @Override
    public String officePreview(AttachOfficePreviewRo officePreviewRo, String spaceId) {
        log.info("Office file preview conversion");
        if (yozoTemplate == null) {
            throw new BusinessException("File preview component is not enabled");
        }
        // Check if the space station has the preview function enabled
        ExceptionUtil.isTrue(
            iAppInstanceService.checkInstanceExist(spaceId, AppType.OFFICE_PREVIEW.name()),
            APP_NOT_OPEN);


        String url = constProperties.spliceAssetUrl(officePreviewRo.getToken());
        // File source address (no suffix)
        String fileUrl = url.contains("?") ? "%s&attname=%s" : "%s?attname=%s";
        // Added URL Ecode encoding to prevent the conversion of special filenames from failing
        try {
            String originUrl = String.format(fileUrl, url,
                officePreviewRo.getAttname().replaceAll("\\s|%|&|\"|\\\\", ""));
            return yozoTemplate.preview(URLEncoder.encode(originUrl, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new BusinessException("Failed to parse filename");
        } catch (YozoApiException exception) {
            exception.printStackTrace();
            throw new BusinessException(ActionException.OFFICE_PREVIEW_GET_URL_FAILED);
        } catch (RestClientException e) {
            log.error("Failed to request office preview server", e);
            throw new BusinessException(ActionException.OFFICE_PREVIEW_API_FAILED);
        }
    }
}
