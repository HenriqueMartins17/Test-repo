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

CREATE TABLE `${table.prefix}integral_history`
(
    `id`              bigint(20)                                                   NOT NULL COMMENT 'Primary Key',
    `user_id`         bigint(20)                                                   NOT NULL COMMENT 'User ID',
    `action_code`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Integral Operation Code',
    `origin_integral` int(11) unsigned                                             NOT NULL COMMENT 'Original Integral Value',
    `alter_type`      tinyint(2) unsigned                                          NOT NULL DEFAULT '0' COMMENT 'Change Type (0: Revenue, 1: Expense)',
    `alter_integral`  int(11) unsigned                                             NOT NULL COMMENT 'Change Integral Value',
    `total_integral`  int(11) unsigned                                             NOT NULL COMMENT 'Change Remain Total Integral Value',
    `created_by`      bigint(20) unsigned                                          NOT NULL COMMENT 'Creator',
    `updated_by`      bigint(20) unsigned                                          NOT NULL COMMENT 'Last Update By',
    `created_at`      timestamp                                                    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at`      timestamp                                                    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Integral System - Integral History Table';
