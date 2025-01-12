package com.apitable.appdata.shared.base.pojo;

import lombok.Data;

@Data
public class LabsFeatures {

    private Long id;

    /**
     * Unique identification of laboratory function
     */
    private String featureKey;

    /**
     * Labs Features Category (user: user level, space: space level)
     */
    private Integer featureScope;

    /**
     * Type of laboratory function (static: no operation, review: can be applied, normal: can be switched on and off normally)
     */
    private Integer type;

    /**
     * Address of experimental function application form
     */
    private String url;

}
