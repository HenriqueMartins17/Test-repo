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

CREATE TABLE `${table.prefix}wechat_keyword_reply`
(
    `id`         bigint(20) unsigned NOT NULL COMMENT 'Primary Key',
    `app_id`     varchar(50)         NOT NULL COMMENT 'Official account Appid（link#xxxx_wechat_authorization#authorizer_appid）',
    `rule_name`  varchar(255)        NOT NULL COMMENT 'Rule Name',
    `match_mode` varchar(50)         NOT NULL COMMENT 'Keyword matching pattern: contain means that the message contains the keyword, and equal means that the message content must be strictly the same as the keyword',
    `reply_mode` varchar(50)         NOT NULL COMMENT 'Reply mode: reply_all represents all replies, and random_one represents one random reply',
    `keyword`    varchar(100)        NOT NULL COMMENT 'Keywords: for text type, content is the text content; for image, picture, voice and video types, content is the media ID',
    `type`       varchar(10)         NOT NULL COMMENT 'The type of automatic reply. The types of automatic reply after attention and automatic reply to messages only support text, image, voice, and video. The automatic reply to keywords includes news',
    `news_info`  json                         DEFAULT NULL COMMENT 'Reply content of graphic message',
    `created_at` timestamp           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `updated_at` timestamp           NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='Third Party System - WeChat Keyword Reply Table';
