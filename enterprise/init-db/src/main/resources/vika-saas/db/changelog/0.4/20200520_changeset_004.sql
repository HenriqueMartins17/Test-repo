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

CREATE TABLE `${table.prefix}content_censor_result`
(
    `id`                 bigint                                                       NOT NULL COMMENT 'Primary Key',
    `node_id`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Node ID',
    `report_num`         bigint unsigned                                              NOT NULL DEFAULT '0' COMMENT 'Report Number',
    `report_result`      tinyint unsigned                                             NOT NULL DEFAULT '0' COMMENT 'Processing results (0: unprocessed, 1: banned, 2: normal (unsealed))',
    `auditor_dt_user_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT 'Auditor DingTalk User ID',
    `auditor_dt_name`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT 'Auditor DingTalk User Name',
    `created_at`         timestamp                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at`         timestamp                                                    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `Idx_Node_id` (`node_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Content Audit Results Record Table';
