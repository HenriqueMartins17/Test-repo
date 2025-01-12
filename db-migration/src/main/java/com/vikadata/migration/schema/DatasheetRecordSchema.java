package com.vikadata.migration.schema;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet_record")
public class DatasheetRecordSchema extends BaseSchema {

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
     * Source ID
     */
    private String sourceId;

    /**
     * source 类型
     */
    private Integer sourceType;

    /**
     * 操作ID
     */
    private String recordId;

    /**
     * 按排序的历史版本号，是原 Operation 的revision，数组下标是当前 record 的 revision
     */
    private List<String> revisionHistory;

    /**
     * 版本号, 该数据不进行迁移，用于标识是否需要进行数据计算(Nest服务进行).
     */
    private Long revision;

    /**
     * 字段更新信息
     */
    private JSONObject recordMeta;

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

    /**
     * 创建用户
     */
    private Long createdBy;

    /**
     * 最后一次更新用户
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

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
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "dataId", "recordId"};
    }

}
