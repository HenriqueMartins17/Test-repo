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

INSERT INTO `billing_subscription` (`id`, `space_id`, `bundle_id`, `subscription_id`, `product_name`,
                                         `product_category`, `plan_id`, `state`, `phase`, `metadata`, `bundle_start_date`,
                                         `start_date`, `expire_date`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`)
VALUES (1526219090054270978, 'spcSueRmAkuPP', '445fcf67-0cf4-4147-85b9-03fbfaaf90c3', 'a953a7c0-e9b1-4392-ace8-60bfe11f89bf',
        'Capacity', 'ADD_ON', 'capacity_300_MB', 'ACTIVATED', 'fixedterm', '{\"userId\":\"123\", \"userName\":\"user1\", \"capacityType\":\"participation_capacity\"}',
        '2022-07-21 00:00:00', '2022-07-21 00:00:00', '2032-01-21 00:00:00', 0, -1, -1, '2022-07-20 15:20:09', '2022-08-17 16:40:44');