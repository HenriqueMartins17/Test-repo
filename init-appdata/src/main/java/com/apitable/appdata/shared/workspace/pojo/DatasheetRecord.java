package com.apitable.appdata.shared.workspace.pojo;

import lombok.Data;

@Data
public class DatasheetRecord {

    private Long id;

    private String dstId;

    private String recordId;

    private String data;

    private String fieldUpdatedInfo;
}
