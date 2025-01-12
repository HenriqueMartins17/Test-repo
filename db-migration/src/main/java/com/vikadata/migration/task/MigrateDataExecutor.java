package com.vikadata.migration.task;

import com.vikadata.migration.config.DataMigrationTaskExecutorConfig;
import com.vikadata.migration.config.MigrationConfig;
import java.util.concurrent.Executor;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MigrateDataExecutor implements ApplicationRunner {

    @Resource(name = DataMigrationTaskExecutorConfig.MIGRATION_EXECUTOR_BEAN_NAME)
    private Executor taskExecutor;

    @Resource
    private MigrationConfig migrationConfig;

    @Resource
    private MigrateSpaceAuditTask migrateSpaceAuditTask;

    @Resource
    private MigrateNodeVisitRecordTask migrateNodeVisitRecordTask;

    @Resource
    private MigrateDatasheetTask migrateDatasheetTask;

    @Resource
    private MigrateDatasheetMetaTask migrateDatasheetMetaTask;

    @Resource
    private MigrateDatasheetRecordTask migrateDatasheetRecordTask;

    @Resource
    private MigrateDatasheetRecordCommentTask migrateDatasheetRecordCommentTask;

    @Resource
    private MigrateDatasheetChangesetTask migrateDatasheetChangesetTask;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (migrationConfig.isEnabled()) {
//            taskExecutor.execute(migrateSpaceAuditTask);
//            taskExecutor.execute(migrateNodeVisitRecordTask);
            taskExecutor.execute(migrateDatasheetTask);
            taskExecutor.execute(migrateDatasheetMetaTask);
            taskExecutor.execute(migrateDatasheetRecordTask);
            taskExecutor.execute(migrateDatasheetRecordCommentTask);
            taskExecutor.execute(migrateDatasheetChangesetTask);
        }
    }
}
