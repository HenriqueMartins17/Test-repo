package com.vikadata.migration.schema;

import cn.hutool.json.JSONArray;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vika_datasheet_operation")
public class DatasheetOperationSchema extends BaseSchema {

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
     * 版本号
     */
    private Long revision;

    /**
     * 操作action的合集
     */
    private JSONArray operations;

}
