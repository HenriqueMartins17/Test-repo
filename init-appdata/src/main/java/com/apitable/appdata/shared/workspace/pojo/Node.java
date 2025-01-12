package com.apitable.appdata.shared.workspace.pojo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class Node {

    private Long id;

    private String spaceId;

    private String parentId;

    private String preNodeId;

    private String nodeId;

    private String nodeName;

    private String icon;

    private Integer type;

    private String cover;

    private Boolean isTemplate;

    private String extra;
}
