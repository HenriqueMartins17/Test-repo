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

import static com.apitable.template.enums.TemplateException.TEMPLATE_CATEGORY_HAVE_BEEN_EXIST;
import static com.apitable.template.enums.TemplateException.TEMPLATE_CATEGORY_NOT_EXIST;

import cn.hutool.core.util.StrUtil;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.enterprise.ops.ro.TemplateCategoryCreateRo;
import com.apitable.enterprise.ops.ro.TemplatePublishRo;
import com.apitable.enterprise.ops.ro.TemplateUnpublishRo;
import com.apitable.enterprise.ops.service.IOpsTemplateService;
import com.apitable.shared.util.IdUtil;
import com.apitable.template.entity.TemplatePropertyEntity;
import com.apitable.template.entity.TemplatePropertyRelEntity;
import com.apitable.template.enums.TemplatePropertyType;
import com.apitable.template.mapper.TemplatePropertyRelMapper;
import com.apitable.template.service.ITemplatePropertyService;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Product Operation System Service Implement Class.
 * </p>
 */
@Service
public class OpsTemplateServiceImpl implements IOpsTemplateService {

    @Resource
    private ITemplatePropertyService iTemplatePropertyService;

    @Resource
    private TemplatePropertyRelMapper templatePropertyRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishTemplate(String templateId, TemplatePublishRo data) {
        TemplatePropertyEntity category =
            iTemplatePropertyService.getTemplateCategory(data.getCategoryCode());
        ExceptionUtil.isNotNull(category, TEMPLATE_CATEGORY_NOT_EXIST);

        List<TemplatePropertyRelEntity> propertyRelEntities =
            templatePropertyRelMapper.selectByPropertyCode(data.getCategoryCode());
        if (propertyRelEntities.isEmpty()) {
            List<String> categoryCodes =
                iTemplatePropertyService.getTemplateCategoryCodeByLang(category.getI18nName());
            if (!categoryCodes.isEmpty()) {
                templatePropertyRelMapper.updateIncPropertyOrderByPropertyCodeIn(categoryCodes);
            }
            TemplatePropertyRelEntity relEntity = new TemplatePropertyRelEntity();
            relEntity.setId(IdWorker.getId());
            relEntity.setTemplateId(templateId);
            relEntity.setPropertyCode(data.getCategoryCode());
            relEntity.setPropertyOrder(0);
            templatePropertyRelMapper.insertBatch(Collections.singletonList(relEntity));
            return;
        }
        boolean pos = propertyRelEntities.size() > data.getIndex();
        TemplatePropertyRelEntity propertyRel =
            pos ? propertyRelEntities.get(data.getIndex())
                : propertyRelEntities.get(propertyRelEntities.size() - 1);
        if (propertyRel.getTemplateId().equals(templateId)) {
            return;
        }
        Long id = pos ? propertyRel.getId() : propertyRel.getId() + 1;
        templatePropertyRelMapper.updateIncIdByIdGreaterThanEqual(id);

        Optional<TemplatePropertyRelEntity> first = propertyRelEntities.stream()
            .filter(rel -> rel.getTemplateId().equals(templateId)).findFirst();
        if (first.isPresent()) {
            templatePropertyRelMapper.updateIdByPropertyCodeAndTemplateId(id,
                data.getCategoryCode(), templateId);
            return;
        }
        TemplatePropertyRelEntity relEntity = new TemplatePropertyRelEntity();
        relEntity.setId(id);
        relEntity.setTemplateId(templateId);
        relEntity.setPropertyCode(propertyRel.getPropertyCode());
        relEntity.setPropertyOrder(propertyRel.getPropertyOrder());
        templatePropertyRelMapper.insertBatch(Collections.singletonList(relEntity));
    }

    @Override
    public void unpublishTemplate(String templateId, TemplateUnpublishRo data) {
        if (Boolean.TRUE.equals(data.getAllCategory())) {
            templatePropertyRelMapper.deleteByTemplateId(templateId);
            return;
        }
        if (StrUtil.isBlank(data.getCategoryCode())) {
            return;
        }
        templatePropertyRelMapper.deleteByPropertyCodeAndTemplateId(data.getCategoryCode(),
            templateId);
    }

    @Override
    public String createTemplateCategory(TemplateCategoryCreateRo data) {
        TemplatePropertyEntity category =
            iTemplatePropertyService.getTemplateCategoryByName(data.getName());
        if (category == null) {
            TemplatePropertyEntity entity = TemplatePropertyEntity.builder()
                .id(IdWorker.getId())
                .propertyType(TemplatePropertyType.CATEGORY.getType())
                .propertyCode(IdUtil.createTempCatCode())
                .propertyName(data.getName())
                .i18nName(data.getI18nName())
                .build();
            iTemplatePropertyService.save(entity);
            return entity.getPropertyCode();
        }
        ExceptionUtil.isTrue(category.getIsDeleted(), TEMPLATE_CATEGORY_HAVE_BEEN_EXIST);
        category.setI18nName(data.getI18nName());
        category.setIsDeleted(false);
        iTemplatePropertyService.updateById(category);
        return category.getPropertyCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateCategory(String categoryCode) {
        TemplatePropertyEntity category =
            iTemplatePropertyService.getTemplateCategory(categoryCode);
        ExceptionUtil.isNotNull(category, TEMPLATE_CATEGORY_NOT_EXIST);
        // delete
        templatePropertyRelMapper.deleteByPropertyCode(categoryCode);
        iTemplatePropertyService.removeById(category.getId());
    }
}
