package com.vikadata.migration.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author Shawn Deng
 * @date 2021-08-17 15:09:44
 */
@ConfigurationProperties("vika")
@Data
public class MultipleTaskProperties {

    private List<ExtensionTaskExecutionProperties> tasks;

    @Getter
    @Setter
    public static class ExtensionTaskExecutionProperties extends TaskExecutionProperties {
    }
}
