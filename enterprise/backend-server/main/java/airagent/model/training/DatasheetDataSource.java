package com.apitable.enterprise.airagent.model.training;

import cn.hutool.json.JSONUtil;
import com.apitable.workspace.dto.DatasheetSnapshot;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * datasheet data source.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DatasheetDataSource extends AbstractDataSource {

    private String id;

    private Setting setting;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.DATASHEET;
    }

    @Override
    public String toSettingRawJson() {
        return setting.toJson();
    }

    /**
     * datasheet setting.
     */
    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Setting extends HutoolJsonObject {

        private String datasheetId;

        private String datasheetName;

        private String viewId;

        private String viewName;

        private Long rows;

        private Long revision;

        private List<DatasheetSnapshot.Field> fields;

        public static Setting fromJsonString(String jsonString) {
            return JSONUtil.toBean(jsonString, Setting.class);
        }
    }
}
