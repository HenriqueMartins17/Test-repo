-- APITable Ltd. <legal@apitable.com>
-- Copyright (C)  2022 APITable Ltd. <https://apitable.com>
--
-- This code file is part of APITable Enterprise Edition.
--
-- It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
--
-- Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
--
-- Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
--
-- For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.

INSERT INTO `wechat_keyword_reply` (`id`, `app_id`, `rule_name`,
                                         `match_mode`, `reply_mode`, `keyword`,
                                         `content`, `type`, `news_info`,
                                         `created_at`, `updated_at`)
VALUES (1310519353805565954, 'app_id', 'scan', 'equal', 'reply_all',
        'keyword', 'test', 'text', NULL, '2020-09-28 17:58:56',
        '2020-09-28 17:58:56');