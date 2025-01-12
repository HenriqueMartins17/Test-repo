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

package com.apitable.enterprise.vika.core.model;

/**
 * user contact information
 */
public class UserContactInfo {

    private String recordId;

    private String uuid;

    private String code;

    private String mobilePhone;

    private String email;

    private final static String USER_NOT_BIND_PHONE = "USER NOT BIND PHONE";

    private final static String USER_NOT_BIND_EMIAL = "USER NOT BIND EMAIL";

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCode() {
        if(code == null){
            return USER_NOT_BIND_PHONE;
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMobilePhone() {
        if(mobilePhone == null){
            return USER_NOT_BIND_PHONE;
        }
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        if(email == null){
            return USER_NOT_BIND_EMIAL;
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}