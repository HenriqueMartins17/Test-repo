package com.apitable.appdata.shared.widget.pojo;

import lombok.Data;

@Data
public class WidgetPackageRelease {

    private Long id;

    private String releaseSha;

    private String version;

    private String packageId;

    private Long releaseUserId;

    private String releaseCodeBundle;

    private String sourceCodeBundle;

    private String secretKey;

    private Integer status;

    private String releaseNote;

    private String installEnvCode;

    private String runtimeEnvCode;

    private Long createdBy;

    private Long updatedBy;
}
