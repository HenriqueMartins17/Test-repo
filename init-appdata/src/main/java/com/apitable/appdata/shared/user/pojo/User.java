package com.apitable.appdata.shared.user.pojo;

import lombok.Data;

@Data
public class User {

    private Long id;

    private String uuid;

    private String nickName;

    private String email;

    private String password;

    private Integer color;

}
