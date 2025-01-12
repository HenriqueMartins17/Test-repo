package com.vikadata.migration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 工作台-数表变更集来源表
 *
 * @author Mybatis Generator Tool
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
@TableName("vika_datasheet_changeset_source")
public class DatasheetChangesetSourceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 数表ID(关联#vika_datasheet#dst_id)
     */
    private String dstId;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * changeset请求的唯一标识，用于保证resource changeset的唯一
     */
    private String messageId;

    /**
     * 数据来源ID
     */
    private String sourceId;

    /**
     * 数据来源类型(0:user_interface,1:openapi,2:relation_effect)
     */
    private Integer sourceType;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
