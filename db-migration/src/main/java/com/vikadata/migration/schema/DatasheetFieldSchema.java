package com.vikadata.migration.schema;

import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet_field")
public class DatasheetFieldSchema extends BaseSchema {

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
     * 数表自定义ID(关联#vika_datasheet#dst_id)
     */
    @JsonProperty("dst_id")
    private String datasheetId;

    /**
     * Field ID
     */
    private String fieldId;

    /**
     * 字段名称
     */
    private String name;

    /**
     * 描述
     */
    private String desc;

    /**
     * 字段类型 1-文本「Text」2-数字「NUMBER」 3-单选 「SINGLESELECT」4-多选「MULTISELECT」 5-日期「DATETIME」 6-附件「ATTACHMENT」 7-关联「LINK」
     */
    private Integer type;

    /**
     * 属性
     */
    private JSONObject property;

    /**
     * 是否设置为表单必填项
     */
    private Boolean required;

    /**
     * 是否首字段
     */
    private Boolean isPrimary;

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
                .and("fieldId").is(this.fieldId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "fieldId", "dataId", "isPrimary", "createdAt"};
    }
}
