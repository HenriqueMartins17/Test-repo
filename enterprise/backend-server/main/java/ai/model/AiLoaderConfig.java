package com.apitable.enterprise.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ai object loader config.
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AiLoaderConfig {

    private boolean anonymous = true;

    private Long userId;

    public static AiLoaderConfig create() {
        return new AiLoaderConfig();
    }
}
