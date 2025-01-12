/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vikadata.scheduler.space.pojo;

import lombok.Data;

@Data
public class Asset {

    private Long id;

    /**
     * Hash, MD5 summary of the entire file
     */
    private String checksum;

    /**
     * Base64 of the first 32 bytes of the resource file
     */
    private String headSum;

    /**
     * Bucket flag
     */
    private String bucket;

    /**
     * bucket name
     */
    private String bucketName;

    /**
     * file size (unit: byte)
     */
    private Integer fileSize;

    /**
     * Cloud file storage path
     */
    private String fileUrl;

    /**
     * MimeType
     */
    private String mimeType;

    /**
     * file extension
     */
    private String extensionName;

    /**
     * preview token
     */
    private String preview;

    /**
     * Whether it is a template attachment (0: no, 1: yes)
     */
    private Boolean isTemplate;

    /**
     * image height
     */
    private Integer height;

    /**
     * Image width
     */
    private Integer width;
}
