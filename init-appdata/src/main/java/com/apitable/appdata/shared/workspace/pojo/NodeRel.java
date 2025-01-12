package com.apitable.appdata.shared.workspace.pojo;

import lombok.Data;

@Data
public class NodeRel {

    private Long id;

    private String mainNodeId;

    private String relNodeId;

    private String extra;
}
