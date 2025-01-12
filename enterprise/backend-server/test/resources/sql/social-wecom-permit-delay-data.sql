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

INSERT INTO `social_wecom_permit_delay` (`id`, `suite_id`, `auth_corp_id`,
                                              `first_auth_time`, `delay_type`, `process_status`)
VALUES (1, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 1, 0),
       (2, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 1, 1),
       (3, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 1, 5),
       (4, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 1, 9),
       (5, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 2, 0),
       (6, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 2, 1),
       (7, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 2, 5),
       (8, 'wwxxx123', 'wwcorpx123123', '2022-02-22 20:22:02', 2, 9);