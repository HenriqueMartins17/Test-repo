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

CREATE TABLE `${table.prefix}social_tenant`
(
    `id`                 bigint(20) unsigned                     NOT NULL COMMENT 'Primary Key',
    `app_id`             varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Application ID',
    `app_type`           tinyint(2) unsigned                     NOT NULL COMMENT 'Application type (1: enterprise internal application, 2: independent service provider)',
    `tenant_id`          varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'The unique identifier of the enterprise. The terms of the major platforms are inconsistent. Tenants are used here to represent',
    `contact_auth_scope` json                                             DEFAULT NULL COMMENT 'Address book permission range',
    `platform`           tinyint(2) unsigned                     NOT NULL COMMENT 'Platform (1: WeCom, 2: DingTalk, 3: Feishu)',
    `status`             tinyint(1) unsigned                     NOT NULL DEFAULT '1' COMMENT 'Status (0: Deactivate, 1: Enable)',
    `created_at`         timestamp                               NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at`         timestamp                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_tenant` (`app_id`, `tenant_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party Platform Integration - Social Tenant Table';