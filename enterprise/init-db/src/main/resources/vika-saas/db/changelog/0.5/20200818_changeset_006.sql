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

CREATE TABLE `${table.prefix}code_usage`
(
    `id`            bigint(20) unsigned                                          NOT NULL COMMENT 'Primary Key',
    `type`          tinyint(2) unsigned                                          NOT NULL COMMENT 'Type (0: Receive; 1: Use)',
    `code`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'V Code',
    `operator`      bigint(20)                                                            DEFAULT NULL COMMENT 'Operator ID(Third party member ID/User ID)',
    `operator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '[Redundancy]Operator Name ',
    `created_at`    timestamp                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_code` (`code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='V Code System - V Code Record Table';
