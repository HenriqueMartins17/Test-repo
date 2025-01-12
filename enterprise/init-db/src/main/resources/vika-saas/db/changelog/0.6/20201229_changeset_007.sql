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

CREATE TABLE `${table.prefix}social_user`
(
    `id`         bigint(20) unsigned                                           NOT NULL COMMENT 'Primary Key',
    `union_id`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'User unique identification on the platform',
    `platform`   tinyint(2) unsigned                                           NOT NULL COMMENT 'Platform (1: WeCom, 2: DingTalk, 3: Feishu)',
    `created_at` timestamp                                                     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at` timestamp                                                     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_platform_union_id` (`union_id`, `platform`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party Platform Integration - User Table';