package com.apitable.appdata.shared.asset.pojo;

import lombok.Data;

@Data
public class Asset {

    private Long id;

    /**
     * MD5
     */
    private String checksum;

    /**
     * Base64 of the first 32 bytes
     */
    private String headSum;

    /**
     * Bucket Mark
     */
    private String bucket;

    /**
     * Bucket Name
     */
    private String bucketName;

    /**
     * File Size(Unit:Byte)
     */
    private Integer fileSize;

    /**
     * Relative Path
     */
    private String fileUrl;

    private String mimeType;

    private String extensionName;

    /**
     * Preview Relative Path
     */
    private String preview;

    private Integer height;

    private Integer width;
}
