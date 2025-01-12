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

INSERT INTO `social_wecom_permit_order_account` (`id`, `suite_id`, `auth_corp_id`,
                                                      `type`, `activate_status`,
                                                      `active_code`, `cp_user_id`,
                                                      `create_time`, `active_time`, `expire_time`)
VALUES (1, 'wwxxx123', 'wwcorpx123123', 1, 2, 'ac1', 'wx123', '2022-07-29 19:12:38', '2022-07-29 19:12:38', '2023-07-29 19:12:38'),
       (2, 'wwxxx123', 'wwcorpx123123', 1, 2, 'ac2', 'wx456', '2022-07-29 19:12:38', '2022-07-29 19:12:38', '2023-07-29 19:12:38');