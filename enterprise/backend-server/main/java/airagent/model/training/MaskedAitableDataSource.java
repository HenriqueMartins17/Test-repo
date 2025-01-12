package com.apitable.enterprise.airagent.model.training;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * agent - airtable data source.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MaskedAitableDataSource extends AbstractDataSource {

    private String id;

    private Setting setting;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.AITABLE;
    }

    @Override
    public String toSettingRawJson() {
        return setting.toJson();
    }

    /**
     * aitable setting.
     */
    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Setting extends HutoolJsonObject {

        private String datasheetId;

        private String viewId;

        public static Setting fromJsonString(String jsonString) {
            return JSONUtil.toBean(jsonString, Setting.class);
        }
    }
}
