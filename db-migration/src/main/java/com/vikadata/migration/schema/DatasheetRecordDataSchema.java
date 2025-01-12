package com.vikadata.migration.schema;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet_record_data")
public class DatasheetRecordDataSchema extends BaseSchema {

    /**
     * 自动生成的ID，非原MySQL表ID
     */
    @Id
    private String id;

    // /**
    //  * Space ID
    //  */
    // private String spaceId;

    /**
     * 数表ID(关联#vika_datasheet#dst_id)
     */
    private String datasheetId;

    /**
     * 操作ID
     */
    private String recordId;

    /**
     * 一行记录的数据（对应每个字段Field）
     */
    private JSONObject data;

    // /**
    //  * 原主键
    //  */
    // @JsonProperty("id")
    // private Long dataId;

    /**
     * 删除标记(0:否,1:是)
     */
    private Boolean isDeleted;

    public Query genBusinessPrimaryKeyQuery() {
        Query query = new Query();
        query.addCriteria(Criteria
                // .where("spaceId").is(this.spaceId)
                .where("datasheetId").is(this.datasheetId)
                .and("recordId").is(this.recordId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id", "sizeExceeded", "duplicated"};
        }
        return new String[]{"_id", "datasheetId", "dataId", "recordId", "sizeExceeded", "duplicated"};
    }

}
