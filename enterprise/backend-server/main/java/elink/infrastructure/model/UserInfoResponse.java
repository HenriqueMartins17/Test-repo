package com.apitable.enterprise.elink.infrastructure.model;


public class UserInfoResponse {

    /**
     * User Id.
     */
    private String UserId;

    private int errcode;


    private String DeviceId;

    /**
     * User type.
     */
    private int usertype;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }
}
