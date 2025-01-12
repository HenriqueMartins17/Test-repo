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

package com.vikadata.scheduler.space.vika;

import java.util.Map;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * <p>
 * Simple execution of Command factory</br>
 * As it is a low-frequency operation, the object model is not defined, and the Json Str data structure is directly used
 * </p>
 *
 */
public class CollaCommandFactory {

    /**
     * Fix Unidirectional Association Custom Cmd</br>
     * Repair by one-way copy and changing the associated foreignDstId</br>
     */
    public static Map<String, Object> fixOneWayLinkByChangeDstId(String dstId, String fieldId, String foreignDstId) {
        String jsonStr = "{\n"
                + "    \"cmd\":\"AddFields\",\n"
                + "    \"copyCell\":true,\n"
                + "    \"internalFix\":{\n"
                + "        \"fixUser\":{\n"
                + "              \"userId\":-1,\n"
                + "              \"uuid\":\"-1\"\n"
                + "         },\n"
                + "        \"selfCreateNewField\":false,\n"
                + "        \"changeOneWayLinkDstId\":true\n"
                + "    },\n"
                + "    \"data\":[\n"
                + "        {\n"
                + "            \"data\":{\n"
                + "                \"type\":7,\n"
                + "                \"property\":{\n"
                + "                    \"foreignDatasheetId\":\"{foreignDatasheetId}\"\n"
                + "                }\n"
                + "            },\n"
                + "            \"fieldId\":\"{fieldId}\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"fieldId\":\"{fieldId}\",\n"
                + "    \"resourceId\":\"{resourceId}\",\n"
                + "    \"resourceType\":0\n"
                + "}";

        Dict dict = Dict.create();
        dict.set("resourceId", dstId)
                .set("fieldId", fieldId)
                .set("foreignDatasheetId", foreignDstId);

        jsonStr = StrUtil.format(jsonStr, dict);

        return JSONUtil.parse(jsonStr).toBean(new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Fix Unidirectional Association Custom Cmd</br>
     * Repair the situation where one-way association and table B do not exist</br>
     * Table B is missing, and the associated column is converted to multiple lines of text</br>
     */
    public static Map<String, Object> fixOneWayLinkByMultilineText(String dstId, String fieldId, String fieldName) {
        String jsonStr = "{\n"
                + "    \"cmd\":\"SetFieldAttr\",\n"
                + "    \"includeLink\": false,\n"
                + "    \"internalFix\":{\n"
                + "        \"fixUser\":{\n"
                + "              \"userId\":-1,\n"
                + "              \"uuid\":\"-1\"\n"
                + "         }\n"
                + "    },\n"
                + "    \"data\":{\n"
                + "        \"id\":\"{fieldId}\",\n"
                + "        \"name\":\"{fieldName}\",\n"
                + "        \"type\":1,\n"
                + "        \"property\":null\n"
                + "    },\n"
                + "    \"fieldId\":\"{fieldId}\",\n"
                + "    \"resourceId\":\"{resourceId}\",\n"
                + "    \"resourceType\":0\n"
                + "}";

        Dict dict = Dict.create();
        dict.set("resourceId", dstId)
                .set("fieldId", fieldId)
                .set("fieldName", fieldName);

        jsonStr = StrUtil.format(jsonStr, dict);

        return JSONUtil.parse(jsonStr).toBean(new TypeReference<Map<String, Object>>() {});
    }

}
