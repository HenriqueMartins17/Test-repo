package com.vikadata.migration.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vika_datasheet_changeset")
public class DatasheetChangesetSchema extends BaseSchema {

    /**
     * 自动生成的ID，非原MySQL表ID
     */
    @Id
    private String id;

     /**
      * 数表ID
      */
     @JsonProperty("dst_id")
     private String datasheetId;

    /**
     * changeSet请求的唯一标识，用于保证changeSet的唯一
     */
    private String messageId;

    /**
     * 操作成员ID(关联#vika_organization_member#id)
     */
    private Long memberId;

    /**
     * 版本号
     */
    private Long revision;


    /**
     * Source ID
     */
    private String sourceId;

    /**
     * source 类型
     */
    private Integer sourceType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 创建用户
     */
    private Long createdBy;

}
