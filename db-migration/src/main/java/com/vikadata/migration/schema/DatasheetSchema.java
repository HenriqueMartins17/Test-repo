package com.vikadata.migration.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet")
public class DatasheetSchema {

    /**
     * 自动生成的ID，非原MySQL表ID
     */
    @Id
    private String id;

    /**
     * Space ID
     */
    private String spaceId;

    /**
     * datasheet ID
     */
    @JsonProperty("dst_id")
    @Indexed(unique = true)
    private String datasheetId;

    /**
     * 版本号
     */
    private Long revision;

    /**
     * view IDs, 用于排序
     */
    private List<String> viewIds;

    /**
     * widget panel IDs, 用于排序
     */
    private List<String> widgetPanelIds;

    /**
     * 是否是模板
     */
    private Boolean isTemplate = false;

    /**
     * 原主键
     */
    @JsonProperty("id")
    private Long dataId;

    /**
     * 1 表示删除，0 表示未删除
     */
    private Boolean isDeleted;


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

    public Query genBusinessPrimaryKeyQuery() {
        Query query = new Query();
        query.addCriteria(Criteria
                // .where("spaceId").is(this.spaceId)
                .where("datasheetId").is(this.datasheetId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "dataId", "isTemplate", "widgetPanelIds", "viewIds"};
    }

}
