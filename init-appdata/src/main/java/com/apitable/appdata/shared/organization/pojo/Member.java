package com.apitable.appdata.shared.organization.pojo;

import lombok.Data;

@Data
public class Member {

    private Long id;

    private Long userId;

    private String spaceId;

    private String memberName;

    private String email;

    /**
     * The user space status (0: inactive; 1: active; 2: pre delete; 3: logout cool down period pre delete)
     */
    private Integer status;

    /**
     * Activate or not (0: No, 1: Yes)
     */
    private Boolean isActive;

    /**
     * Administrator or not (0: No, 1: Yes)
     */
    private Boolean isAdmin;

}
