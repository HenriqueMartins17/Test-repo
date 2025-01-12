package com.vikadata.migration.schema;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Data
@Document(collection = "vika_datasheet_view")
@CompoundIndexes({
        @CompoundIndex(name = "unq_dst_id_view_id", def = "{ datasheetId : 1, viewId : 1 }", unique = true)
})
public class DatasheetViewSchema extends BaseSchema {

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
     * 视图ID
     */
    private String viewId;

    /**
     * 视图名称
     */
    private String name;

    /**
     * 视图「行」
     */
    private JSONArray rows;

    /**
     * 视图「列」
     */
    private JSONArray columns;

    /**
     * 视图属性
     */
    private String property;

    /**
     * 视图类型 1-数表「Grid」
     */
    private Integer type;

    /**
     * 视图描述
     */
    private String description;

    /**
     * 冻结视图列数，从第一列开始，默认为1
     */
    private Integer frozenColumnCount;

    /**
     * 视图隐藏选项
     */
    private Boolean hidden;

    /**
     * 筛选项
     */
    private JSONObject filterInfo;

    /**
     * 排序
     */
    private Object sortInfo;

    /**
     * 行高
     */
    private Integer rowHeightLevel;

    /**
     * 分组
     */
    private JSONArray groupInfo;

    /**
     * 相册视图样式
     */
    private JSONObject style;

    /**
     * 自动保存
     */
    private Boolean autoSave;

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
                .and("viewId").is(this.viewId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "viewId", "dataId"};
    }
}
