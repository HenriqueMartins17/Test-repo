package com.vikadata.migration.dto;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ViewMapDto {

    private String id;

    private String name;

    private JSONArray rows;

    private JSONArray columns;

    private String property;

    private Integer type;

    private String description;

    private Integer frozenColumnCount;

    private Boolean hidden;

    private JSONObject filterInfo;

    private JSONObject sortInfo;

    private Integer rowHeightLevel;

    private JSONArray groupInfo;

    private JSONObject style;

    private Boolean autoSave;
}
