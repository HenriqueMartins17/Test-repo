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

INSERT INTO `wechat_mp_qrcode` (`id`, `app_id`, `type`, `scene`, `ticket`,
                                                `expire_seconds`, `url`, `is_deleted`,
                                                `created_by`, `updated_by`)
VALUES (41, 'wx41', 'QR_STR_SCENE', 'test', 'ticket', 600, 'url', 0, 41, 41);