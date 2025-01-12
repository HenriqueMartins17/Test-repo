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

INSERT INTO `wechat_authorization` (`id`, `authorizer_appid`,
                                                    `authorizer_access_token`,
                                                    `access_token_expire`,
                                                    `authorizer_refresh_token`,
                                                    `nick_name`, `avatar`, `service_type`,
                                                    `verify_type`, `user_name`, `alias`,
                                                    `principal_name`, `business_info`,
                                                    `qrcode_url`, `signature`,
                                                    `miniprograminfo`, `created_at`,
                                                    `updated_at`)
VALUES (1285128373641363457, 'wx3ccd2f6264309a7c',
        '35_IOAR3Xqf_SgNlJo_jvwPs2OyFPR2MXDJeao8Fx8OEnst5xx--jlm7F3HoSao1kB_bSz66A7nuzAa7ABlnLH3GZspyBc6rRzC0aaz6ikiIomjIqsOaKtznaOfhi3SCOJZLnRiMbOG8prETY9vNPPhAKDTVV',
        7200, 'refreshtoken@@@QBSfj6uUiW7reI7G-LNng1pHFZiW_PSBVWgOzfzz3pU', NULL,
        'http://wx.qlogo.cn/mmopen/x9ZibRq4mhLT4Yic5lUqYSkLSYribMbiaD3S72vq0dMlXvZDLHiaIIx8Ct93YkhNhrVX3dyCxSU0RXY57jM2Q92iaXN2JvzFX0FLz9/0',
        2, NULL, 'gh_79ff060614ae', NULL, 'test company',
        '{\"open_pay\": 0, \"open_card\": 0, \"open_scan\": 0, \"open_shake\": 0, \"open_store\": 0}',
        'http://mmbiz.qpic.cn/sz_mmbiz_jpg/VOOfK7ktJCxf0deZCd0YroEf2VlaObU3joKEecfHZ0uvSoc0eRYnNSicwITAgrwqG3K3afRr03icxRd9fXSycuow/0',
        'test company', NULL,
        '2020-07-20 16:24:15', '2020-07-20 16:24:15');