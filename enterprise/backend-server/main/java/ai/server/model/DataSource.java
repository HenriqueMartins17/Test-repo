package com.apitable.enterprise.ai.server.model;

import com.apitable.workspace.model.Datasheet;
import java.util.Map;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * data source.
 */
@Data
public class DataSource {

    private String type;
    private String typeId;
    private Meta meta;
    private Long words;
    private Long characters;
    private Long tokens;

    @Getter
    @Setter
    static class Meta {
        private long count;
        private Map<String, Datasheet.Field> fields;
        private long revision;
    }
}
