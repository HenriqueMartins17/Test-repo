package com.apitable.enterprise.license.model;

import lombok.Data;

@Data
public class LicenseInfo {

    // For licenseString
    private String signature;

    private License license;

    private String  licenseString;

    @Data
    public class License {

        private String customer;

        private int  userSeat;

        /**
         * TimeMillis()
         */
        private Long createdAt;

        /**
         * TimeMillis()
         */
        private Long expireAt;
    }

}
