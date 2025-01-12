package com.apitable.enterprise.airagent.model.training;

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
public class AirtableDataSource extends AbstractDataSource {

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

        private String apiKey;

        private String baseId;

        private String tableId;
    }
}
