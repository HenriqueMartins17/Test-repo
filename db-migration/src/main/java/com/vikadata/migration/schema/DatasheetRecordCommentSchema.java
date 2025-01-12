package com.vikadata.migration.schema;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
@Document(collection = "vika_datasheet_record_comment")
public class DatasheetRecordCommentSchema {

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
    private String datasheetId;

    /**
     * 记录ID
     */
    private String recordId;

    /**
     * chengeset生成的comment_id
     */
    private String commentId;

    /**
     * 评论富文本内容
     */
    private JSONObject commentMsg;

    /**
     * 记录版本号
     */
    private Long revision;

    /**
     * 原主键
     */
    @JsonProperty("id")
    private Long dataId;

    /**
     * 删除标记(0:否,1:是)
     */
    @TableLogic
    private Boolean isDeleted;

    /**
     * 操作用户组织ID(关联#vika_unit#id)
     */
    private Long unitId;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 最后修改者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
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
                .and("recordId").is(this.recordId)
                .and("commentId").is(this.commentId));
        return query;
    }

    public String[] upsertExcludedColumns(boolean insert) {
        if (insert) {
            return new String[]{"_id"};
        }
        return new String[]{"_id", "datasheetId", "dataId", "recordId", "commentId"};
    }
}
