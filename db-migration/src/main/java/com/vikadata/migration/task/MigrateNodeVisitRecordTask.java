package com.vikadata.migration.task;

import com.vikadata.migration.config.MigrationConfig;
import com.vikadata.migration.dto.MigrationOnceDto;
import com.vikadata.migration.schema.DataMigrationCursor;
import com.vikadata.migration.service.DataMigrationCursorService;
import com.vikadata.migration.service.NodeVisitRecordService;
import javax.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MigrateNodeVisitRecordTask implements Runnable {

    @Resource
    private DataMigrationCursorService migrationCursorService;

    @Resource
    private NodeVisitRecordService nodeVisitRecordService;

    @Resource
    private MigrationConfig migrationConfig;

    /**
     * 数据迁移
     */
    @SneakyThrows
    @Override
    public void run() {

        DataMigrationCursor migrationCursor = migrationCursorService
            .getMigrationCursor(NodeVisitRecordService.COLLECTION_NAME);

        String lastMigrateId = null == migrationCursor ? null : migrationCursor.getCursorId();

        log.info("start to migrate node visit record from {} ", lastMigrateId);

        while (true) {
            String start = lastMigrateId;
            try {
                MigrationOnceDto migrationOnceDto = nodeVisitRecordService
                    .migrateData(lastMigrateId, migrationConfig.getLimitCount());
                lastMigrateId = migrationOnceDto.getLastMigrationId();
                if (lastMigrateId.equals(start)) {
                    log.info("There is no node visit record needs to be migrated, wait for 10s");
                    Thread.sleep(10000);
                }
                migrationCursorService.updateCursorId(NodeVisitRecordService.COLLECTION_NAME
                    , lastMigrateId, migrationOnceDto.getMigrationCount(), false);
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                Thread.sleep(3000);
            }
            if (!lastMigrateId.equals(start)) {
                log.info("migrate node_recently_browsed from {} to {}", start, lastMigrateId);
            }
        }
    }

}
