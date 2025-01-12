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

package com.apitable.enterprise.ops.service;

import com.apitable.enterprise.ops.ro.TemplateCategoryCreateRo;
import com.apitable.enterprise.ops.ro.TemplatePublishRo;
import com.apitable.enterprise.ops.ro.TemplateUnpublishRo;

/**
 * <p>
 * Product Operation System Service.
 * </p>
 */
public interface IOpsTemplateService {

    /**
     * Publish template in specified template category.
     *
     * @param templateId    template custom ID
     * @param data          template publish ro
     */
    void publishTemplate(String templateId, TemplatePublishRo data);

    /**
     * UnPublish template.
     *
     * @param templateId    template custom ID
     * @param data          template unpublish ro
     */
    void unpublishTemplate(String templateId, TemplateUnpublishRo data);

    /**
     * Create template category.
     */
    String createTemplateCategory(TemplateCategoryCreateRo data);

    /**
     * Delete template category.
     */
    void deleteTemplateCategory(String categoryCode);
}
