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

CREATE TABLE `${table.prefix}third_party_member`
(
    `id`          bigint(20) unsigned NOT NULL COMMENT 'Primary Key',
    `app_id`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Application ID',
    `type`        tinyint(2) unsigned                                           DEFAULT NULL COMMENT 'Type (0: WeChat applet; 1: WeChat official account)',
    `open_id`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Unique identification of the application',
    `union_id`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Unique identification of the platform',
    `session_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Session Key ',
    `mobile`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT 'Mobile Phone Number',
    `nick_name`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Nick Name',
    `avatar`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'Avatar',
    `extra`       json                                                          DEFAULT NULL COMMENT 'Other Information',
    `created_at`  timestamp           NOT NULL                                  DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at`  timestamp           NULL                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party System - Member Information Table';
