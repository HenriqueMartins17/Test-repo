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

INSERT INTO `billing_order` (`id`, `space_id`, `order_id`,
                                             `order_channel`, `channel_order_id`,
                                             `order_type`, `currency`, `amount`, `state`,
                                             `created_time`, `is_paid`, `paid_time`,
                                             `is_deleted`, `remark`, `version`,
                                             `created_by`, `created_at`, `updated_by`,
                                             `updated_at`)
VALUES (1526219090054270978, 'spc0j9nFgewEN', '20220516231241494813', 'vika', NULL, 'BUY',
        'CNY', 188800, 'finished', '2022-05-16 23:12:42', 1, '2022-05-16 23:14:42', 0,
        NULL, 1, 1526219087906787329, '2022-05-16 15:12:39', 1526219087906787329,
        '2022-05-16 15:12:40');