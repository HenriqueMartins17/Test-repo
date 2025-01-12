package com.vikadata.migration.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("migration")
@Data
public class MigrationConfig {

    private boolean enabled;

    private int limitCount = 200;

    private int metaLimitCount = 50;

    private int changesetLimitCount = 10;

    private Long endDatasheetPid;

    private Long endDatasheetMetaPid;

    private Long endDatasheetRecordPid;

    private Long endDatasheetRecordCommentPid;

    private Long endDatasheetChangesetPid;

    public boolean shouldContinueDatasheetMigration(Long lastMigrationId) {
        return shouldContinueMigration(this.endDatasheetPid, lastMigrationId);
    }

    public boolean shouldContinueMetaMigration(Long lastMigrationId) {
        return shouldContinueMigration(this.endDatasheetMetaPid, lastMigrationId);
    }

    public boolean shouldContinueRecordMigration(Long lastMigrationId) {
        return shouldContinueMigration(this.endDatasheetRecordPid, lastMigrationId);
    }

    public boolean shouldContinueCommentMigration(Long lastMigrationId) {
        return shouldContinueMigration(this.endDatasheetRecordCommentPid, lastMigrationId);
    }

    private boolean shouldContinueMigration(Long endPid, Long lastMigrationId) {
        return null == endPid || 0l == endPid || lastMigrationId < endPid;
    }

}
