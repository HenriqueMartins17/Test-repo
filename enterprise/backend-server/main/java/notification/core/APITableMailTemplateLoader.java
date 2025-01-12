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

package com.apitable.enterprise.notification.core;

import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ACCEPT_INVITE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ADD_SUB_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ASSIGN_GROUP;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ASSIGN_ROLE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_CHANGE_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_DATASHEET_REMIND;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_INVITE_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_MEMBER_APPLY_CLOSE_ACCOUNT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_RECORD_COMMENT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REGISTER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_MEMBER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_ROLE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_SUB_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY_APPROVE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY_REFUSE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_BETA_FEATURE_APPLY_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_ARCHIVED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_CELL_UPDATED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_COMMENTED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_UNARCHIVED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_TASK_REMINDER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_VERIFY_CODE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_QUALIFICATION_AUTH_FAIL;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_QUALIFICATION_AUTH_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_SUBMIT_FAIL;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_SUBMIT_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_TRANSFER_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_UNPUBLISH_GLOBAL_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_UNPUBLISH_NOTIFY;

import java.util.HashMap;
import java.util.Map;

public class APITableMailTemplateLoader {

    public static Long getTemplateId(String subject) {
        return Singleton.INSTANCE.getTemplateId(subject);
    }

    private enum Singleton {
        INSTANCE;

        private final Map<String, Long> en = new HashMap<>();

        Singleton() {
            en.put(SUBJECT_CHANGE_ADMIN, 63499L);
            en.put(SUBJECT_INVITE_NOTIFY, 63497L);
            en.put(SUBJECT_REGISTER, 63494L);
            en.put(SUBJECT_RECORD_COMMENT, 63477L);
            en.put(SUBJECT_DATASHEET_REMIND, 63472L);
            en.put(SUBJECT_REMOVE_MEMBER, 63471L);
            en.put(SUBJECT_SPACE_APPLY, 63467L);
            en.put(SUBJECT_SPACE_APPLY_APPROVE, 86394L);
            en.put(SUBJECT_SPACE_APPLY_REFUSE, 86395L);
            en.put(SUBJECT_VERIFY_CODE, 63466L);

            en.put(SUBJECT_TASK_REMINDER, 66842L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_COMMENTED, 66841L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_CELL_UPDATED, 66840L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_ARCHIVED, 86706L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_UNARCHIVED, 86815L);

            en.put(SUBJECT_WIDGET_TRANSFER_NOTIFY, 70133L);
            en.put(SUBJECT_WIDGET_UNPUBLISH_NOTIFY, 70134L);
            en.put(SUBJECT_WIDGET_QUALIFICATION_AUTH_FAIL, 70136L);
            en.put(SUBJECT_WIDGET_SUBMIT_SUCCESS, 70139L);
            en.put(SUBJECT_WIDGET_SUBMIT_FAIL, 70140L);
            en.put(SUBJECT_WIDGET_UNPUBLISH_GLOBAL_NOTIFY, 70141L);
            en.put(SUBJECT_WIDGET_QUALIFICATION_AUTH_SUCCESS, 70138L);

            en.put(SUBJECT_ADD_SUB_ADMIN, 86393L);
            en.put(SUBJECT_REMOVE_SUB_ADMIN, 86391L);
            en.put(SUBJECT_ASSIGN_GROUP, 86392L);
            en.put(SUBJECT_ASSIGN_ROLE, 86400L);
            en.put(SUBJECT_REMOVE_ROLE, 86399L);
            en.put(SUBJECT_ACCEPT_INVITE, 86390L);
            en.put(SUBJECT_MEMBER_APPLY_CLOSE_ACCOUNT, 86397L);
            en.put(SUBJECT_SPACE_BETA_FEATURE_APPLY_SUCCESS, 86396L);
        }

        public Long getTemplateId(String subject) {
            return en.get(subject);
        }
    }
}
