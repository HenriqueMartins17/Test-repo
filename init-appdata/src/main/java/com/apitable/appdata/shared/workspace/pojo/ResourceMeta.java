package com.apitable.appdata.shared.workspace.pojo;

import lombok.Data;

@Data
public class ResourceMeta {

    private Long id;

    private String resourceId;

    /**
     * Type(0:DATASHEET; 1:FORM; 2:DASHBOARD; 3:WIDGET; 4:MIRROR)
     */
    private Integer resourceType;

    private String metaData;
}
