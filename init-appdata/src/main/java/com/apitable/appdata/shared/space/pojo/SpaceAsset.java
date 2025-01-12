package com.apitable.appdata.shared.space.pojo;

import lombok.Data;

@Data
public class SpaceAsset {

    private Long id;

    private String spaceId;

    private String nodeId;

    private Long assetId;

    private String assetChecksum;

    private Integer cite;

    /**
     * Type(0:Avatar; 1:Space Logo; 2:Datasheet Attachment; 3:Cover; 4:Description)
     */
    private Integer type;

    /**
     * File Source Name
     */
    private String sourceName;

    /**
     * [Redundant] File Size(Unit:Byte)
     */
    private Integer fileSize;
}
