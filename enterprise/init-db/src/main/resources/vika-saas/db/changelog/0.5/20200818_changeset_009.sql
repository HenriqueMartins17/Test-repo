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

CREATE TABLE `${table.prefix}wechat_mp_log`
(
    `id`           bigint(20) unsigned NOT NULL COMMENT 'Primary Key',
    `app_id`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Application ID',
    `open_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Unique identification within open applications',
    `union_id`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Unique ID in the developer enterprise',
    `msg_type`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Message Type',
    `event_type`   varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Event Type',
    `scene`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Scene Values',
    `extra`        json                                                          DEFAULT NULL COMMENT 'Other Information',
    `creator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '[Redundancy]Creator Name',
    `created_at`   timestamp           NOT NULL                                  DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party System - WeChat Mp Log Table';
