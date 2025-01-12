package com.apitable.enterprise.ai.model;

import com.apitable.interfaces.ai.model.AiType;
import lombok.Data;

/**
 * AI node object.
 *
 * @author Shawn Deng
 */
@Data
public class AiObject {

    private String spaceId;
    private String aiId;
    private AiType type;
    private String name;
    private String description;
    private String picture;
    private String prologue;
    private String prompt;
    private String setting;
}
