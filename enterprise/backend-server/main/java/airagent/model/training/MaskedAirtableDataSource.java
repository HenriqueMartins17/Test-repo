package com.apitable.enterprise.airagent.model.training;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * masked airtable data source.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class MaskedAirtableDataSource extends AbstractDataSource {

    private String id;

    private Setting setting;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.AIRTABLE;
    }

    @Override
    public String toSettingRawJson() {
        return setting.toJson();
    }

    /**
     * airtable setting.
     */
    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Setting extends HutoolJsonObject {

        private String baseId;

        private String tableId;

        public static Setting fromJsonString(String jsonString) {
            return JSONUtil.toBean(jsonString, Setting.class);
        }
    }
}
