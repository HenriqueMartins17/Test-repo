package com.vikadata.migration.schema;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "vika_data_migration_cursor")
public class DataMigrationCursor {
    /**
     * 主键
     */
    @Id
    private String id;
    /**
     * 表名
     */
    private String table;

    /**
     * 最后迁移的数据的Primary ID
     */
    private String cursorId = null;

    /**
     * 待迁移数据量
     */
    private Long originalCount = 0l;

    /**
     * 已迁移数据量
     */
    private Long migratedCount = 0l;

    /**
     * 是否已完成
     */
    private Boolean finished = false;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt = LocalDateTime.now();

    public double migrationProgress() {
        if (0l == originalCount) {
            return 0;
        }
        return Math.round(migratedCount / (double)originalCount * 10000) / 100.0;
    }
}
