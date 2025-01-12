/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License
 *  and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory
 * and its subdirectories does not constitute permission to use this code
 * or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.ops.service.impl;

import static com.apitable.base.enums.DatabaseException.DELETE_ERROR;
import static com.apitable.base.enums.DatabaseException.EDIT_ERROR;
import static com.apitable.base.enums.DatabaseException.INSERT_ERROR;
import static com.apitable.space.enums.LabsApplicantTypeEnum.SPACE_LEVEL_FEATURE;
import static com.apitable.space.enums.LabsApplicantTypeEnum.USER_LEVEL_FEATURE;
import static com.apitable.space.enums.LabsFeatureEnum.ROBOT;
import static com.apitable.space.enums.LabsFeatureEnum.WIDGET_CENTER;
import static com.apitable.space.enums.LabsFeatureException.LAB_FEATURE_HAVE_BEEN_EXIST;
import static com.apitable.space.enums.LabsFeatureException.LAB_FEATURE_NOT_EXIST;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.ops.service.IOpsLabFeatureService;
import com.apitable.shared.component.notification.NotificationManager;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.space.entity.LabsApplicantEntity;
import com.apitable.space.entity.LabsFeaturesEntity;
import com.apitable.space.enums.LabsFeatureEnum;
import com.apitable.space.enums.LabsFeatureException;
import com.apitable.space.enums.LabsFeatureScopeEnum;
import com.apitable.space.enums.LabsFeatureTypeEnum;
import com.apitable.space.mapper.LabsFeatureMapper;
import com.apitable.space.service.ILabsApplicantService;
import com.apitable.space.service.ILabsFeatureService;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Product Operation System - Lab Feature Service Implement Class.
 * </p>
 */
@Service
public class OpsLabFeatureServiceImpl implements IOpsLabFeatureService {

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ILabsFeatureService iLabsFeatureService;

    @Resource
    private LabsFeatureMapper labsFeatureMapper;

    @Resource
    private ILabsApplicantService iLabsApplicantService;

    @Override
    public void create(LabsFeatureEnum feature, LabsFeatureScopeEnum scope,
                       LabsFeatureTypeEnum type, String url) {
        LabsFeaturesEntity labFeature =
            labsFeatureMapper.selectByFeatureKeyAndFeatureScope(feature.getFeatureName(),
                scope.getScopeCode());
        ExceptionUtil.isNull(labFeature, LAB_FEATURE_HAVE_BEEN_EXIST);
        LabsFeaturesEntity entity = LabsFeaturesEntity.builder()
            .featureKey(feature.name())
            .featureScope(scope.getScopeCode())
            .type(type.getType())
            .url(url)
            .build();
        boolean flag = SqlHelper.retBool(labsFeatureMapper.insert(entity));
        ExceptionUtil.isTrue(flag, INSERT_ERROR);
    }

    @Override
    public void edit(String featureKey, String scope, String type, String url) {
        // Unique identification of verification experimental function
        LabsFeatureEnum feature = LabsFeatureEnum.ofLabsFeature(featureKey);
        ExceptionUtil.isFalse(Objects.equals(feature, LabsFeatureEnum.UNKNOWN_LAB_FEATURE),
            LabsFeatureException.FEATURE_KEY_IS_NOT_EXIST);
        // Check the number of modified values of attributes
        ExceptionUtil.isFalse(StrUtil.isAllBlank(scope, type, url),
            LabsFeatureException.FEATURE_ATTRIBUTE_AT_LEAST_ONE);
        LabsFeaturesEntity labsFeaturesEntity = new LabsFeaturesEntity();
        // Verify the scope of experimental functions
        if (StrUtil.isNotBlank(scope)) {
            LabsFeatureScopeEnum scopeEnum = LabsFeatureScopeEnum.ofLabsFeatureScope(scope);
            ExceptionUtil.isFalse(Objects.equals(scopeEnum, LabsFeatureScopeEnum.UNKNOWN_SCOPE),
                LabsFeatureException.FEATURE_SCOPE_IS_NOT_EXIST);
            labsFeaturesEntity.setFeatureScope(scopeEnum.getScopeCode());
        }
        // Verify experimental function type
        if (StrUtil.isNotBlank(type)) {
            LabsFeatureTypeEnum labsFeatureTypeEnum =
                LabsFeatureTypeEnum.ofLabsFeatureType(type);
            ExceptionUtil.isFalse(
                Objects.equals(labsFeatureTypeEnum, LabsFeatureTypeEnum.UNKNOWN_LABS_FEATURE_TYPE),
                LabsFeatureException.FEATURE_TYPE_IS_NOT_EXIST);
            labsFeaturesEntity.setType(labsFeatureTypeEnum.getType());
        }
        // Modify laboratory function properties
        Long featureId = iLabsFeatureService.getIdByFeatureKey(featureKey);
        ExceptionUtil.isNotNull(featureId, LabsFeatureException.FEATURE_KEY_IS_NOT_EXIST);
        labsFeaturesEntity.setId(featureId);
        labsFeaturesEntity.setUrl(url);
        boolean flag = iLabsFeatureService.updateById(labsFeaturesEntity);
        ExceptionUtil.isTrue(flag, EDIT_ERROR);
    }

    @Override
    public void remove(String featureKey) {
        LabsFeatureEnum feature = LabsFeatureEnum.ofLabsFeature(featureKey);
        ExceptionUtil.isFalse(Objects.equals(feature, LabsFeatureEnum.UNKNOWN_LAB_FEATURE),
            LabsFeatureException.FEATURE_KEY_IS_NOT_EXIST);
        LabsFeaturesEntity entity =
            labsFeatureMapper.selectByFeatureKey(feature.name());
        ExceptionUtil.isNotNull(entity, LAB_FEATURE_NOT_EXIST);
        boolean flag = iLabsFeatureService.removeById(entity.getId());
        ExceptionUtil.isTrue(flag, DELETE_ERROR);
    }

    @Override
    public void apply(String featureKey, String uuid, String spaceId) {
        // If the feature Key is incorrect, the application for opening is not allowed.
        LabsFeatureEnum feature = LabsFeatureEnum.ofLabsFeature(featureKey);
        ExceptionUtil.isFalse(Objects.equals(feature, LabsFeatureEnum.UNKNOWN_LAB_FEATURE),
            LabsFeatureException.FEATURE_KEY_IS_NOT_EXIST);

        Long applyUserId = iUserService.getUserIdByUuidWithCheck(uuid);
        String applicant;
        int applicantType;
        if (StrUtil.isNotBlank(spaceId)) {
            // Verify the space id. If the space id is illegal, it cannot be opened
            iSpaceService.checkExist(spaceId);
            applicant = spaceId;
            applicantType = SPACE_LEVEL_FEATURE.getCode();
        } else {
            applicant = String.valueOf(applyUserId);
            applicantType = USER_LEVEL_FEATURE.getCode();
        }
        LabsApplicantEntity existLabsApplicant =
            iLabsApplicantService.getApplicantByApplicantAndFeatureKey(applicant, feature.name());
        if (existLabsApplicant != null) {
            return;
        }
        LabsApplicantEntity labsApplicant = LabsApplicantEntity.builder()
            .applicantType(applicantType)
            .applicant(applicant)
            .featureKey(feature.name())
            .createdBy(applyUserId)
            .build();
        boolean flag = iLabsApplicantService.save(labsApplicant);
        ExceptionUtil.isTrue(flag, INSERT_ERROR);
        // Send space notification after successful opening.(members except applicant)
        this.sendNotification(
            NotificationTemplateId.APPLY_SPACE_BETA_FEATURE_SUCCESS_NOTIFY_ALL,
            Collections.singletonList(applyUserId), applyUserId, feature, spaceId);
        // Send space notification after successful opening. (applicant)
        this.sendNotification(
            NotificationTemplateId.APPLY_SPACE_BETA_FEATURE_SUCCESS_NOTIFY_ME,
            Collections.singletonList(applyUserId), 0L, feature, spaceId);
    }

    public void sendNotification(NotificationTemplateId templateId, List<Long> toUserId,
                                 Long applyUser, LabsFeatureEnum featureEnum, String spaceId) {
        String toastUrl = null;
        if (featureEnum == ROBOT) {
            toastUrl = "/help/manual-vika-robot";
        } else if (featureEnum == WIDGET_CENTER) {
            toastUrl = "/help/intro-widget-center";
        }
        NotificationManager.me().playerNotify(
            templateId,
            toUserId,
            applyUser,
            spaceId,
            Dict.create()
                .set("toast",
                    Dict.create().set("url", StrUtil.isNotBlank(toastUrl) ? toastUrl : null))
                .set("featureKey", featureEnum.getFeatureName())
                .set("FEATURE_NAME", I18nStringsUtil.t(featureEnum.getFeatureName(),
                    LocaleContextHolder.getLocale()))
        );
    }
}
