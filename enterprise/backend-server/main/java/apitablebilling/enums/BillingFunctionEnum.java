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

package com.apitable.enterprise.apitablebilling.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * Billing Function
 * </p>
 *
 * @author Shawn Deng
 */
@Getter
@AllArgsConstructor
public enum BillingFunctionEnum {

    API_CALL("api_call"),

    CAPACITY("storage_capacity"),

    SEATS("seats"),

    ADMIN_NUM("space_admin"),

    ROWS_PER_SHEET("rows_limit"),

    ARCHIVED_ROWS_PER_SHEET("archived_rows_limit"),

    TOTAL_SHEET_ROWS("space_rows_limit"),

    TRASH("trash"),

    NODES("nodes"),

    CALENDAR_VIEW("calendar_view"),

    GALLERY_VIEW("gallery_view"),

    GANTT_VIEW("gantt_view"),

    KANBAN_VIEW("kanban_view"),

    FORM_VIEW("form_view"),

    FIELD_PERMISSION("field_permission"),

    NODE_PERMISSIONS("node_permissions"),

    TIME_MACHINE("time_machine"),

    RAINBOW_LABEL("rainbow_label"),

    WATERMARK("watermark"),

    RECORD_ACTIVITY("activity"),

    SECURITY_SETTING_INVITE_MEMBER("security_setting_invite_member"),

    SECURITY_SETTING_APPLY_JOIN_SPACE("security_setting_apply_join_space"),

    SECURITY_SETTING_SHARE("security_setting_share"),

    SECURITY_SETTING_EXPORT("security_setting_export"),

    SECURITY_SETTING_DOWNLOAD_FILE("security_setting_download_file"),

    SECURITY_SETTING_COPY_CELL_DATA("security_setting_copy_cell_data"),

    SECURITY_SETTING_MOBILE("security_setting_mobile"),

    AUDIT_QUERY("audit_query"),

    SECURITY_SETTING_ADDRESS_LIST_ISOLATION("security_setting_address_list_isolation"),

    SECURITY_SETTING_CATALOG_MANAGEMENT("security_setting_catalog_management"),

    MIRRORS("mirrors"),

    EMBED("embed_links"),

    SOCIAL_CONNECT("social_connect"),

    API_QPS("api_qps"),

    ORG_API("org_api"),

    MESSAGE_WIDGET("message_widget"),

    MESSAGE_AUTOMATION_RUN("message_automation_run"),

    AI_AGENT_NUMS("ai_agent_nums"),

    MESSAGE_CREDIT("message_credit"),

    CONTROL_FORM_BRAND_LOGO("control_form_brand_logo")

    ;

    private final String code;

    public static BillingFunctionEnum of(String code) {
        for (BillingFunctionEnum e : BillingFunctionEnum.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new NullPointerException("Can't Get Subscription Plan Function");
    }
}
