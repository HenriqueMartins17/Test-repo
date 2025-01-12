CREATE TABLE `vika_social_tenant`
(
    `id`                 bigint unsigned                                               NOT NULL COMMENT 'primary key',
    `app_id`             varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'app ID',
    `app_type`           tinyint unsigned                                              NOT NULL COMMENT 'app type(1: internal, 2: isv)',
    `tenant_id`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'corp ID',
    `contact_auth_scope` json                                                                   DEFAULT NULL COMMENT 'contact auth scope',
    `auth_info`          json                                                                   DEFAULT NULL COMMENT 'auth info',
    `status`             tinyint unsigned                                              NOT NULL DEFAULT '1' COMMENT 'status(0: disable, 1: enable)',
    `created_at`         timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    `updated_at`         timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `is_deleted`         tinyint unsigned                                              NOT NULL DEFAULT '0' COMMENT 'deleted 1:yes 0: no',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_app_tenant` (`app_id`, `tenant_id`) USING BTREE,
    KEY `k_tenant_id` (`tenant_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='tenant info table';

CREATE TABLE `vika_ding_talk_open_sync_biz_data`
(
    `id`           bigint unsigned NOT NULL COMMENT 'primary key',
    `subscribe_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'suiteid underlined 0',
    `corp_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'corp id',
    `biz_type`     int unsigned    NOT NULL COMMENT 'biz type',
    `biz_id`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'biz id',
    `biz_data`     json                                                         DEFAULT NULL COMMENT 'biz data',
    `created_at`   timestamp       NOT NULL                                     DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    `updated_at`   timestamp       NULL                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_subscribe_id` (`subscribe_id`) USING BTREE COMMENT 'subscribe_id key',
    KEY `idx_corp_id` (`corp_id`) USING BTREE COMMENT 'corp_id key'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='syncHttp high priority event data';

CREATE TABLE `vika_ding_talk_open_sync_biz_data_medium`
(
    `id`           bigint unsigned NOT NULL COMMENT 'primary key',
    `subscribe_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'suiteid underlined 0',
    `corp_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'corp id',
    `biz_type`     int unsigned    NOT NULL COMMENT 'biz type',
    `biz_id`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'biz id',
    `biz_data`     json                                                         DEFAULT NULL COMMENT 'biz data',
    `created_at`   timestamp       NOT NULL                                     DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    `updated_at`   timestamp       NULL                                         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_subscribe_id` (`subscribe_id`) USING BTREE COMMENT 'subscribe_id key',
    KEY `idx_corp_id` (`corp_id`) USING BTREE COMMENT 'corp_id key'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='syncHttp medium priority event data';

CREATE TABLE `vika_social_tenant_department`
(
    `id`                        bigint(20) unsigned                                           NOT NULL COMMENT 'primary key',
    `tenant_id`                 varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'corp id',
    `open_department_id`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT 'department open ID',
    `parent_open_department_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT 'department parent open ID',
    `department_name`           varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT 'department name',
    `department_order`          int(11)                                                                DEFAULT '0' COMMENT 'department order',
    `created_at`                timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    `updated_at`                timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `k_tenant_id` (`tenant_id`) USING BTREE COMMENT 'tenant_id key',
    KEY `k_open_department_id` (`open_department_id`) USING BTREE COMMENT 'open_department_id key'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='tenant department table';

CREATE TABLE `vika_social_tenant_user`
(
    `id`         bigint(20) unsigned                                           NOT NULL COMMENT 'primary key',
    `tenant_id`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'corp id',
    `open_id`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'platform user id',
    `union_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT 'platform union id in isv',
    `created_at` timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    `updated_at` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_tenant_user` (`tenant_id`, `open_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='tenant user table';