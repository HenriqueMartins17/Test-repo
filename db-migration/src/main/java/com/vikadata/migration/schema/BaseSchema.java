package com.vikadata.migration.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class BaseSchema {

    /***
     * 是否超过mongo document最大
     */
    @Transient
    private Boolean sizeExceeded = false;

    /***
     * 是否重复数据
     */
    @Transient
    private Boolean duplicated = false;

    /**
     * Space ID
     */
    @Field(order = 1)
    private String spaceId;

    @JsonProperty("id")
    private Long dataId;
}
