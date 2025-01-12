package com.vikadata.migration.task;

import com.vikadata.migration.config.MigrationConfig;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.schema.DataMigrationCursor;
import com.vikadata.migration.service.DataMigrationCursorService;
import com.vikadata.migration.service.DatasheetChangesetService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MigrateDatasheetChangesetTask implements Runnable {

    @Resource
    private DataMigrationCursorService migrationCursorService;

    @Resource
    private DatasheetChangesetService service;

    @Resource
    private MigrationConfig migrationConfig;

    /**
     * 数据迁移
     */
    @SneakyThrows
    @Override
    public void run() {

        DataMigrationCursor migrationCursor = migrationCursorService
            .getMigrationCursor(DatasheetChangesetService.COLLECTION_NAME);

        String lastMigrateId = null == migrationCursor ? null : migrationCursor.getCursorId();

        log.info("start to migrate datasheet_changeset from {} ", lastMigrateId);

        while (true) {
            String start = lastMigrateId;
            try {
                MigrationOnceDto migrationOnceDto = service
                    .migrateData(lastMigrateId, migrationConfig.getLimitCount());
                lastMigrateId = migrationOnceDto.getLastMigrationId();
                if (lastMigrateId.equals(start)) {
                    log.info("migrate datasheet_changeset finished");
                    migrationCursorService.updateCursorId(DatasheetChangesetService.COLLECTION_NAME
                        , lastMigrateId, migrationOnceDto.getMigrationCount(), true);
                    break;
                }
                migrationCursorService.updateCursorId(DatasheetChangesetService.COLLECTION_NAME
                    , lastMigrateId, migrationOnceDto.getMigrationCount(), false);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                Thread.sleep(3000);
            }
            if (!lastMigrateId.equals(start)) {
                log.info("migrate datasheet_changeset from {} to {}", start, lastMigrateId);
            }
        }
    }

}
