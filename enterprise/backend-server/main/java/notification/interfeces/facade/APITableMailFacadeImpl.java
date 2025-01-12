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

package com.apitable.enterprise.notification.interfeces.facade;

import com.apitable.enterprise.notification.core.APITableMailTemplateLoader;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * APITable Mail Facade Implement Class.
 *
 * @author Chambers
 */
public class APITableMailFacadeImpl extends AbstractEnterpriseMailFacade {

    private static final String RESOURCE_PATH_PREFIX =
        "templates/notification/enterprise/apitable-saas";

    /**
     * * Get Cloud Mail Template Id.
     *
     * @param lang    language
     * @param subject mail subject
     * @return template id about cloud mail
     */
    @Override
    public Long getCloudMailTemplateId(final String lang, final String subject) {
        return APITableMailTemplateLoader.getTemplateId(subject);
    }

    /**
     * * Get Subject Properties.
     *
     * @return Properties
     */
    @Override
    public Properties getSubjectProperties(final String locale) throws IOException {
        return super.getProperties(RESOURCE_PATH_PREFIX, locale, Locale.US.toLanguageTag());
    }

    /**
     * * Load Template Resource Path.
     *
     * @param locale       locale
     * @param templateName templateName
     * @return Path
     */
    @Override
    public String loadTemplateResourcePath(final String locale, final String templateName) {
        return super.loadTemplateResourcePath(RESOURCE_PATH_PREFIX,
            templateName, locale, Locale.US.toLanguageTag());
    }
}
