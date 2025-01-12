package com.vikadata.migration.dto;

import cn.hutool.json.JSONObject;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
public class OperationMapDto {

    private  String cmd;

    private List<Object> actions;
}
