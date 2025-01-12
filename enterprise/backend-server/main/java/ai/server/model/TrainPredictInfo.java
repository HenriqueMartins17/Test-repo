package com.apitable.enterprise.ai.server.model;

import com.apitable.workspace.model.Datasheet;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Map;
import lombok.Data;

/**
 * train predict info.
 */
@Data
public class TrainPredictInfo {

    private String type;

    private String typeId;

    private Long words;

    private Long characters;

    private Long tokens;

    private Long count;

    private Map<String, Datasheet.Field> fields;

    private Long revision;
}
