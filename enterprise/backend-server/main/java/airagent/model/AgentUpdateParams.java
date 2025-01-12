package com.apitable.enterprise.airagent.model;

import com.apitable.enterprise.airagent.model.training.DataSourceCreateParams;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * agent update params.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AgentUpdateParams {

    private String name;

    private Map<String, Object> setting;

    private DataSourceCreateParams dataSources;
}
