package com.apitable.enterprise.airagent.model.training;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * file data source.
 */
@Setter
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FileDataSource extends AbstractDataSource {

    private String id;

    private Setting setting;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public DataSourceType getType() {
        return DataSourceType.FILE;
    }

    @Override
    public String toSettingRawJson() {
        return setting.toJson();
    }

    /**
     * file setting.
     */
    @Getter
    @Setter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Setting extends HutoolJsonObject {

        private String name;

        private String url;

        private Long size;

        private Long numOfCharacters;

        public static Setting fromJsonString(String jsonString) {
            return JSONUtil.toBean(jsonString, Setting.class);
        }
    }
}
