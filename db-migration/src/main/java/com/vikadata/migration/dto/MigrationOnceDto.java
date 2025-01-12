package com.vikadata.migration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MigrationOnceDto {

    /**
     * 待迁移数量
     */
    private Integer migrationCount = 0;

    /**
     * 迁移成功数量
     */
    private Integer successCount = 0;

    /**
     * 最后迁移的ID
     */
    private String lastMigrationId;

    public boolean finished() {
        return 0 == this.migrationCount;
    }

}
