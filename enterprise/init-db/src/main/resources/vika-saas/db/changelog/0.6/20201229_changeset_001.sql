-- APITable <https://github.com/apitable/apitable>
-- Copyright (C) 2022 APITable Ltd. <https://apitable.com>
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU Affero General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.

CREATE TABLE `${table.prefix}social_feishu_event_log`
(
    `id`         bigint(20) unsigned                                           NOT NULL COMMENT 'Primary Key',
    `app_id`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Application ID',
    `tenant_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Enterprise ID',
    `uuid`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT 'Event unique identifier',
    `ts`         varchar(255) COLLATE utf8mb4_unicode_ci                                DEFAULT NULL COMMENT 'Event Send Time',
    `type`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT 'Event Type',
    `event_data` json                                                                   DEFAULT NULL COMMENT 'Event content',
    `status`     tinyint(1) unsigned                                                    DEFAULT '0' COMMENT 'Processed or not (0: No, 1: Yes)',
    `created_at` timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_uuid` (`uuid`) COMMENT 'UUID unique'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party Platform Integration - Feishu Event Log Table';