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
public class SpaceAsset {

    private Long id;

    /**
     * Space ID (associate #vika_space#space_id)
     */
    private String spaceId;

    /**
     * Number table node Id (association #vika_node#node_id)
     */
    private String nodeId;

    /**
     * Resource ID (associate #vika_asset#id)
     */
    private Long assetId;

    /**
     * [redundant] md5 digest
     */
    private String assetChecksum;

    /**
     * Citations
     */
    private Integer cite;

    /**
     * Type (0: user avatar 1: space logo2: data table attachment 3: thumbnail image
     * 4: node description image)
     */
    private Integer type;

    /**
     * Source file name, the file name uploaded this time
     */
    private String sourceName;

    /**
     * [Redundant] file size (unit: byte)
     */
    private Integer fileSize;

    /**
     * [Redundant] Whether it is a template attachment (0: No, 1: Yes)
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