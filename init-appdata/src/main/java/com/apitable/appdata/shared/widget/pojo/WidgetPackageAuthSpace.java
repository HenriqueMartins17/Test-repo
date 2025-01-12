package com.apitable.appdata.shared.widget.pojo;

import lombok.Data;

@Data
public class WidgetPackageAuthSpace {

    private Long id;

    private String packageId;

    private String spaceId;

    private Integer type;

    private Integer widgetSort;

    private Long createdBy;

    private Long updatedBy;

}
