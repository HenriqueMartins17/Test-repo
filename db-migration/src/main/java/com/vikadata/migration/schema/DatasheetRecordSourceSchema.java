package com.vikadata.migration.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vika_datasheet_record_resource")
public class DatasheetRecordSourceSchema {

    /**
     * 自动生成的ID，非原MySQL表ID
     */
    @Id
    private String id;

    /**
     * 数表ID(关联#vika_datasheet#dst_id)
     */
    @JsonProperty("dst_id")
    private String datasheetId;

    /**
     * 数据记录ID(关联#vika_datasheet_record#record_id)
     */
    private String recordId;

    /**
     * 来源ID
     */
    private String sourceId;

    /**
     * 数据来源类型(0:user_interface,1:openapi,2:relation_effect)
     */
    private Integer type;

    /**
     * 原主键
     */
    @JsonProperty("id")
    private Long dataId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建用户
     */
    private Long createdBy;

    /**
     * 最后一次更新用户
     */
    private Long updatedBy;
}
