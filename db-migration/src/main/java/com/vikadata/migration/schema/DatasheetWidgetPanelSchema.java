package com.vikadata.migration.schema;

import cn.hutool.json.JSONArray;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet_widget_panel")
public class DatasheetWidgetPanelSchema extends BaseSchema {

    /**
     * 自动生成的ID，非原MySQL表ID
     */
    @Id
    @JsonIgnore
    private ObjectId _id;

    // /**
    //  * Space ID
    //  */
    // private String spaceId;

    /**
     * 数表自定义ID(关联#vika_datasheet#dst_id)
     */
    private String datasheetId;

    /**
     * widgetPanel业务ID
     */
    private String widgetPanelId;

    /**
     * 名称
     */
    private String name;

    /**
     * 筛选项
     */
    private JSONArray widgets;

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
                .and("widgetPanelId").is(this.widgetPanelId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "widgetPanelId", "dataId"};
    }
}
