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

INSERT INTO `idaas_user_bind` (`id`,
                                    `tenant_name`,
                                    `user_id`,
                                    `nick_name`,
                                    `email`,
                                    `mobile`,
                                    `group_ids`,
                                    `vika_user_id`,
                                    `is_deleted`,
                                    `created_at`,
                                    `updated_at`)
VALUES (1537680923155570689,
        'test-20220617',
        'us-fd0a293b0c934707ba744682418d2685',
        'test-20220617',
        NULL,
        NULL,
        NULL,
        1537680923096850434,
        0,
        '2022-06-17 14:21:37',
        '2022-06-17 14:21:37'),
       (1537680923155570690,
        'test-20220617',
        'us-9bf50b5d19554ae597397ead5e9ebb27',
        'test-20220617',
        'lisi@test-20220617.com',
        NULL,
        '["pe-5c90877121b14b2585f10df9633c311f", "pe-db7d96524a994726a8d5d980938cf371"]',
        1537680923105239041,
        1,
        '2022-06-17 14:21:37',
        '2022-06-17 14:21:37'),
       (1537680923155570691,
        'test-20220617',
        'us-4c268792ff00402e967a22b5310d38ee',
        'test20220617',
        'zhangsan@test-20220617.com',
        NULL,
        '["pe-2eb05e5ab15c4b0a8aed5089c06dbb9a", "pe-db7d96524a994726a8d5d980938cf371"]',
        1537680923105239043,
        0,
        '2022-06-17 14:21:37',
        '2022-06-17 14:21:37');