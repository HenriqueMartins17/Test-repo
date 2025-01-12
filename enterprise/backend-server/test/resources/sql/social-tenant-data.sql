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

INSERT INTO `social_tenant` (`id`, `app_id`, `app_type`, `tenant_id`,
                                  `contact_auth_scope`, `auth_mode`,
                                  `permanent_code`, `auth_info`, `platform`,
                                  `status`)
VALUES (41, 'ai41', 1, 'ww41', NULL, 1, NULL,
        '{"appAuthInfo": {"agentId": "41", "appName": "test" }}',
        2, 1);
INSERT INTO `social_tenant` (`id`, `app_id`, `app_type`, `tenant_id`,
                                  `contact_auth_scope`, `auth_mode`,
                                  `permanent_code`, `auth_info`, `platform`,
                                  `status`)
VALUES (45, 'ai45', 2, 'ww45', NULL, 1, NULL,
        '{"authInfo": {"agent": [{"appid": "ai45", "agentid": "45", "agentName": "test"}]}}',
        2, 1);