package com.apitable.appdata.shared.organization.pojo;

import lombok.Data;

@Data
public class Unit {

    private Long id;

    private String unitId;

    private String spaceId;

    /**
     * Type (1: Department, 2: Label, 3: Member)
     */
    private Integer unitType;

    private Long unitRefId;

}
