package com.apitable.appdata.shared.widget.pojo;

import lombok.Data;

@Data
public class WidgetPackage {

    private Long id;

    private String packageId;

    private String i18nName;

    private String i18nDescription;

    private String icon;

    private String cover;

    private Integer status;

    private Integer installedNum;

    private String authorName;

    private String authorEmail;

    private String authorIcon;

    private String authorLink;

    private Integer packageType;

    private Integer releaseType;

    private String widgetBody;

    private Boolean sandbox;

    private Long releaseId;

    private Boolean isTemplate;

    private Boolean isEnabled;

    private String installEnvCode;

    private String runtimeEnvCode;

    private Long owner;

    private Long createdBy;

    private Long updatedBy;

}
