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

package com.apitable.enterprise.notification.core;

import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ACCEPT_INVITE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ADD_RECORD_LIMITED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ADD_RECORD_SOON_LIMITED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ADD_SUB_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ASSIGN_GROUP;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_ASSIGN_ROLE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_AUTOMATION_ERROR;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_CAPACITY_FULL;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_CHANGE_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_DATASHEET_REMIND;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_INVITE_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_MEMBER_APPLY_CLOSE_ACCOUNT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_PAI_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_RECORD_COMMENT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REGISTER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_MEMBER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_ROLE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_REMOVE_SUB_ADMIN;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY_APPROVE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_APPLY_REFUSE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_BETA_FEATURE_APPLY_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_CERTIFICATION_FAIL_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SPACE_CERTIFICATION_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_ADMIN_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_API_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_CALENDAR_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_CAPACITY_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_DATASHEET_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_DATASHEET_RECORD_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_FIELD_PERMISSION_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_FILE_PERMISSION_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_FORM_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_GANNT_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_MIRROR_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_ARCHIVED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_CELL_UPDATED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_COMMENTED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_RECORD_UNARCHIVED;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_SUBSCRIBED_SEATS_LIMIT;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_TASK_REMINDER;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_VERIFY_CODE;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WARN_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_QUALIFICATION_AUTH_FAIL;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_QUALIFICATION_AUTH_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_SUBMIT_FAIL;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_SUBMIT_SUCCESS;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_TRANSFER_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_UNPUBLISH_GLOBAL_NOTIFY;
import static com.apitable.shared.constants.MailPropConstants.SUBJECT_WIDGET_UNPUBLISH_NOTIFY;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VikaMailTemplateLoader {

    public static Long getTemplateId(String lang, String subject) {
        if (Locale.US.toLanguageTag().equals(lang)) {
            return US.INSTANCE.getTemplateId(subject);
        }
        return Chinese.INSTANCE.getTemplateId(subject);
    }

    private enum Chinese {
        INSTANCE;

        private final Map<String, Long> chinese = new HashMap<>();

        Chinese() {
            chinese.put(SUBJECT_CHANGE_ADMIN, 26507L);
            chinese.put(SUBJECT_INVITE_NOTIFY, 26497L);
            chinese.put(SUBJECT_REGISTER, 27397L);
            chinese.put(SUBJECT_RECORD_COMMENT, 23466L);
            chinese.put(SUBJECT_DATASHEET_REMIND, 23467L);
            chinese.put(SUBJECT_REMOVE_MEMBER, 23471L);
            chinese.put(SUBJECT_SPACE_APPLY, 26509L);
            chinese.put(SUBJECT_SPACE_APPLY_APPROVE, 86382L);
            chinese.put(SUBJECT_SPACE_APPLY_REFUSE, 86385L);
            chinese.put(SUBJECT_VERIFY_CODE, 27395L);

            chinese.put(SUBJECT_PAI_SUCCESS, 23462L);
            chinese.put(SUBJECT_CAPACITY_FULL, 23465L);
            chinese.put(SUBJECT_WIDGET_TRANSFER_NOTIFY, 23468L);
            chinese.put(SUBJECT_WIDGET_UNPUBLISH_NOTIFY, 23469L);
            chinese.put(SUBJECT_WARN_NOTIFY, 23533L);
            chinese.put(SUBJECT_ADD_RECORD_SOON_LIMITED, 23956L);
            chinese.put(SUBJECT_ADD_RECORD_LIMITED, 24007L);
            chinese.put(SUBJECT_WIDGET_SUBMIT_SUCCESS, 24533L);
            chinese.put(SUBJECT_WIDGET_SUBMIT_FAIL, 24534L);
            chinese.put(SUBJECT_WIDGET_QUALIFICATION_AUTH_SUCCESS, 24532L);
            chinese.put(SUBJECT_WIDGET_QUALIFICATION_AUTH_FAIL, 24531L);
            chinese.put(SUBJECT_WIDGET_UNPUBLISH_GLOBAL_NOTIFY, 25076L);
            chinese.put(SUBJECT_TASK_REMINDER, 25038L);
            chinese.put(SUBJECT_SUBSCRIBED_RECORD_CELL_UPDATED, 25336L);
            chinese.put(SUBJECT_SUBSCRIBED_RECORD_COMMENTED, 25334L);
            chinese.put(SUBJECT_SUBSCRIBED_RECORD_UNARCHIVED, 86817L);
            chinese.put(SUBJECT_SUBSCRIBED_RECORD_ARCHIVED, 86709L);
            chinese.put(SUBJECT_SUBSCRIBED_DATASHEET_LIMIT, 52372L);
            chinese.put(SUBJECT_SUBSCRIBED_DATASHEET_RECORD_LIMIT, 52375L);
            chinese.put(SUBJECT_SUBSCRIBED_CAPACITY_LIMIT, 52378L);
            chinese.put(SUBJECT_SUBSCRIBED_SEATS_LIMIT, 52379L);
            chinese.put(SUBJECT_SUBSCRIBED_RECORD_LIMIT, 52382L);
            chinese.put(SUBJECT_SUBSCRIBED_API_LIMIT, 52383L);
            chinese.put(SUBJECT_SUBSCRIBED_CALENDAR_LIMIT, 52386L);
            chinese.put(SUBJECT_SUBSCRIBED_FORM_LIMIT, 52387L);
            chinese.put(SUBJECT_SUBSCRIBED_MIRROR_LIMIT, 52390L);
            chinese.put(SUBJECT_SUBSCRIBED_GANNT_LIMIT, 52391L);
            chinese.put(SUBJECT_SUBSCRIBED_FIELD_PERMISSION_LIMIT, 52394L);
            chinese.put(SUBJECT_SUBSCRIBED_FILE_PERMISSION_LIMIT, 52395L);
            chinese.put(SUBJECT_SUBSCRIBED_ADMIN_LIMIT, 52398L);
            chinese.put(SUBJECT_ACCEPT_INVITE, 86363L);
            chinese.put(SUBJECT_ASSIGN_GROUP, 86373L);
            chinese.put(SUBJECT_ADD_SUB_ADMIN, 86369L);
            chinese.put(SUBJECT_REMOVE_SUB_ADMIN, 86377L);
            chinese.put(SUBJECT_SPACE_BETA_FEATURE_APPLY_SUCCESS, 86370L);
            chinese.put(SUBJECT_MEMBER_APPLY_CLOSE_ACCOUNT, 86378L);
            chinese.put(SUBJECT_SPACE_CERTIFICATION_NOTIFY, 86389L);
            chinese.put(SUBJECT_SPACE_CERTIFICATION_FAIL_NOTIFY, 86386L);
            chinese.put(SUBJECT_ASSIGN_ROLE, 86374L);
            chinese.put(SUBJECT_REMOVE_ROLE, 86381L);
            chinese.put(SUBJECT_AUTOMATION_ERROR, 96083L);

        }

        public Long getTemplateId(String subject) {
            return chinese.get(subject);
        }
    }

    private enum US {
        INSTANCE;

        private final Map<String, Long> en = new HashMap<>();

        US() {
            en.put(SUBJECT_CHANGE_ADMIN, 26508L);
            en.put(SUBJECT_INVITE_NOTIFY, 26498L);
            en.put(SUBJECT_REGISTER, 23614L);
            en.put(SUBJECT_RECORD_COMMENT, 23617L);
            en.put(SUBJECT_DATASHEET_REMIND, 23618L);
            en.put(SUBJECT_REMOVE_MEMBER, 23622L);
            en.put(SUBJECT_SPACE_APPLY, 26510L);
            en.put(SUBJECT_SPACE_APPLY_APPROVE, 86383L);
            en.put(SUBJECT_SPACE_APPLY_REFUSE, 86384L);
            en.put(SUBJECT_VERIFY_CODE, 23612L);

            en.put(SUBJECT_PAI_SUCCESS, 23462L);
            en.put(SUBJECT_CAPACITY_FULL, 23616L);
            en.put(SUBJECT_WIDGET_TRANSFER_NOTIFY, 23619L);
            en.put(SUBJECT_WIDGET_UNPUBLISH_NOTIFY, 23620L);
            en.put(SUBJECT_ADD_RECORD_SOON_LIMITED, 23957L);
            en.put(SUBJECT_ADD_RECORD_LIMITED, 24008L);
            en.put(SUBJECT_WIDGET_SUBMIT_SUCCESS, 25083L);
            en.put(SUBJECT_WIDGET_SUBMIT_FAIL, 25084L);
            en.put(SUBJECT_WIDGET_UNPUBLISH_GLOBAL_NOTIFY, 25082L);
            en.put(SUBJECT_TASK_REMINDER, 25039L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_CELL_UPDATED, 25337L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_COMMENTED, 25335L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_UNARCHIVED, 86816L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_ARCHIVED, 86707L);
            en.put(SUBJECT_SUBSCRIBED_DATASHEET_LIMIT, 52373L);
            en.put(SUBJECT_SUBSCRIBED_DATASHEET_RECORD_LIMIT, 52376L);
            en.put(SUBJECT_SUBSCRIBED_CAPACITY_LIMIT, 52377L);
            en.put(SUBJECT_SUBSCRIBED_SEATS_LIMIT, 52380L);
            en.put(SUBJECT_SUBSCRIBED_RECORD_LIMIT, 52381L);
            en.put(SUBJECT_SUBSCRIBED_API_LIMIT, 52384L);
            en.put(SUBJECT_SUBSCRIBED_CALENDAR_LIMIT, 52385L);
            en.put(SUBJECT_SUBSCRIBED_FORM_LIMIT, 52388L);
            en.put(SUBJECT_SUBSCRIBED_MIRROR_LIMIT, 52389L);
            en.put(SUBJECT_SUBSCRIBED_GANNT_LIMIT, 52392L);
            en.put(SUBJECT_SUBSCRIBED_FIELD_PERMISSION_LIMIT, 52393L);
            en.put(SUBJECT_SUBSCRIBED_FILE_PERMISSION_LIMIT, 52396L);
            en.put(SUBJECT_SUBSCRIBED_ADMIN_LIMIT, 52397L);
            en.put(SUBJECT_ACCEPT_INVITE, 86364L);
            en.put(SUBJECT_ASSIGN_GROUP, 86372L);
            en.put(SUBJECT_ADD_SUB_ADMIN, 86365L);
            en.put(SUBJECT_REMOVE_SUB_ADMIN, 86376L);
            en.put(SUBJECT_SPACE_BETA_FEATURE_APPLY_SUCCESS, 86371L);
            en.put(SUBJECT_MEMBER_APPLY_CLOSE_ACCOUNT, 86379L);
            en.put(SUBJECT_SPACE_CERTIFICATION_NOTIFY, 86388L);
            en.put(SUBJECT_SPACE_CERTIFICATION_FAIL_NOTIFY, 86387L);
            en.put(SUBJECT_ASSIGN_ROLE, 86375L);
            en.put(SUBJECT_REMOVE_ROLE, 86380L);
            en.put(SUBJECT_AUTOMATION_ERROR, 96081L);
        }

        public Long getTemplateId(String subject) {
            return en.get(subject);
        }
    }
}
