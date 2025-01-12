package com.vikadata.migration.dto;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class FieldMapDto {

    private String id;

    private String name;

    private String desc;

    private Integer type;

    private JSONObject property;

    private Boolean required;
}
