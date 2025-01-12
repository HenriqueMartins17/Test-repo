package com.vikadata.migration.task;

import com.vikadata.migration.config.MigrationConfig;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.schema.DataMigrationCursor;
import com.vikadata.migration.service.DataMigrationCursorService;
import com.vikadata.migration.service.SpaceAuditService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MigrateSpaceAuditTask implements Runnable {

    @Resource
    private DataMigrationCursorService migrationCursorService;

    @Resource
    private SpaceAuditService spaceAuditService;

    @Resource
    private MigrationConfig migrationConfig;

    /**
     * 数据迁移
     */
    @SneakyThrows
    @Override
    public void run() {

        DataMigrationCursor migrationCursor = migrationCursorService
            .getMigrationCursor(SpaceAuditService.COLLECTION_NAME);

        String lastMigrateId = null == migrationCursor ? null : migrationCursor.getCursorId();

        log.info("start to migrate space audit from {} ", lastMigrateId);

        while (true) {
            String start = lastMigrateId;
            try {
                MigrationOnceDto migrationOnceDto = spaceAuditService
                    .migrateData(lastMigrateId, migrationConfig.getLimitCount());
                lastMigrateId = migrationOnceDto.getLastMigrationId();
                if (lastMigrateId.equals(start)) {
                    log.info("There is no space audit needs to be migrated, wait for 10s");
                    Thread.sleep(10000);
                }
                migrationCursorService.updateCursorId(SpaceAuditService.COLLECTION_NAME
                    , lastMigrateId, migrationOnceDto.getMigrationCount(), false);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                Thread.sleep(3000);
            }
            if (!lastMigrateId.equals(start)) {
                log.info("migrate audit_space from {} to {}", start, lastMigrateId);
            }
        }
    }

}
