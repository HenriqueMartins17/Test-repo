package com.apitable.enterprise.airagent.model.training;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * data source create params.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DataSourceCreateParam {

    @JsonDeserialize(using = DataSourceTypeDeserializer.class)
    private DataSourceType type;

    private Airtable airtable;

    private Aitable aitable;

    private File file;

    private Datasheet datasheet;

    /**
     * airtable data source.
     */
    @Setter
    @Getter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Airtable {

        private String apiKey;

        private String baseId;

        private String tableId;
    }

    /**
     * aitable data source.
     */
    @Setter
    @Getter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Aitable {

        private String apiKey;

        private String datasheetId;

        private String viewId;
    }

    /**
     * file data source.
     */
    @Setter
    @Getter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class File {

        private String name;

        private String url;
    }

    /**
     * file data source.
     */
    @Setter
    @Getter
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Datasheet {

        private String datasheetId;

        private String viewId;
    }

    static class DataSourceTypeDeserializer extends JsonDeserializer<DataSourceType> {

        @Override
        public DataSourceType deserialize(JsonParser p, DeserializationContext context)
            throws IOException {
            String value = p.getValueAsString();
            try {
                return DataSourceType.of(value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
}
