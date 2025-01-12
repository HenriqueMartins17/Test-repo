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

INSERT INTO `billing_social_wecom_order` (`id`, `order_id`, `order_status`,
                                               `order_type`,
                                               `paid_corp_id`, `operator_id`,
                                               `suite_id`, `edition_id`, `price`,
                                               `user_count`,
                                               `order_period`, `order_time`, `paid_time`,
                                               `begin_time`, `end_time`, `order_from`,
                                               `operator_corp_id`, `service_share_amount`,
                                               `platform_share_amount`,
                                               `dealer_share_amount`, `dealer_corp_id`,
                                               `order_info`, `created_by`, `updated_by`)
VALUES (1, 'testOrderId1', 0, 1, 'testPaidCorpId', 'testOperatorId', 'testSuiteId',
        'testEditionId', 10000, 10, 365, '2022-08-29 12:14:42', '2022-08-29 12:14:42',
        '2022-08-29 12:14:42', '2023-08-29 12:14:42', 0, 'testOperatorCorpId', 9000, 1000,
        0, NULL, '{}', -1, -1),
       (2, 'testOrderId2', 0, 2, 'testPaidCorpId', 'testOperatorId', 'testSuiteId',
        'testEditionId', 10000, 10, 365, '2022-08-29 12:14:42', '2022-08-29 12:14:42',
        '2022-08-29 12:14:42', '2023-08-29 12:14:42', 0, 'testOperatorCorpId', 9000, 1000,
        0, NULL, '{}', -1, -1),
       (3, 'testOrderId3', 1, 1, 'testPaidCorpId', 'testOperatorId', 'testSuiteId',
        'testEditionId', 10000, 10, 365, '2022-08-29 12:14:42', '2022-08-29 12:14:42',
        '2022-08-29 12:14:42', '2023-08-29 12:14:42', 0, 'testOperatorCorpId', 9000, 1000,
        0, NULL, '{}', -1, -1),
       (4, 'testOrderId4', 1, 2, 'testPaidCorpId', 'testOperatorId', 'testSuiteId',
        'testEditionId', 10000, 10, 365, '2022-08-29 12:14:42', '2022-08-29 12:14:42',
        '2022-08-29 12:14:42', '2023-08-29 12:14:42', 0, 'testOperatorCorpId', 9000, 1000,
        0, NULL, '{}', -1, -1);