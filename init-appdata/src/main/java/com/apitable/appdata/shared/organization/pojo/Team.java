package com.apitable.appdata.shared.organization.pojo;

import lombok.Data;

@Data
public class Team {

    private Long id;

    private String spaceId;

    private Long parentId;

    private String teamName;

}
