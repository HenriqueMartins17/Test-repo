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

package com.apitable.enterprise.social.constants;

/**
 *  Lark constant definition
 * @author Shawn Deng
 */
public class LarkConstants {

    public static final String SPACE_WORKBENCH_URL = "/space/%s/workbench";

    public static String formatSpaceWorkbenchUrl(String spaceId) {
        return String.format(SPACE_WORKBENCH_URL, spaceId);
    }

    public static final String ISV_ENTRY_URL = "/api/v1/social/feishu/entry";

    public static final String INTERNAL_ENTRY_URL = "/api/v1/lark/idp/entry/%s";

    public static String formatInternalEntryUrl(String appInstanceId) {
        return String.format(INTERNAL_ENTRY_URL, appInstanceId);
    }

    public static final String INTERNAL_LOGIN_URL = "/api/v1/lark/idp/login/%s";

    public static String formatInternalLoginUrl(String appInstanceId) {
        return String.format(INTERNAL_LOGIN_URL, appInstanceId);
    }

    public static final String INTERNAL_EVENT_URL = "/api/v1/lark/event/%s";

    public static String formatInternalEventUrl(String appInstanceId) {
        return String.format(INTERNAL_EVENT_URL, appInstanceId);
    }

    public static final String CONTACT_SYNCING_URL = "/user/lark/integration/sync/%s";

    public static String formatContactSyncingUrl(String appInstanceId) {
        return String.format(CONTACT_SYNCING_URL, appInstanceId);
    }

    public static final String CONFIG_ERROR_URL = "/user/lark/config/error";
}
