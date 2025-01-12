package com.apitable.enterprise.ai.model;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI update params.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AiUpdateParams {

    private String name;
    private String picture;
    private String description;
    private Map<String, Object> setting;
    private List<DataSourceParam> dataSources;

    @Setter
    @Getter
    public static class DataSourceParam {

        private String nodeId;
    }
}
