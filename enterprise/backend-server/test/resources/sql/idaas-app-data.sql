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

INSERT INTO `idaas_app` (`id`,
                              `tenant_name`,
                              `client_id`,
                              `client_secret`,
                              `authorization_endpoint`,
                              `token_endpoint`,
                              `userinfo_endpoint`,
                              `is_deleted`,
                              `created_at`,
                              `updated_at`)
VALUES (1537679125736923137,
        'test-20220617',
        'ai-cf92ca1c0ac24777a0a13ddc0f49a724',
        '9XbyklblYCjkwsUPUTlVYCLu',
        'https://test-20220617-idp.cig.tencentcs.com/sso/tn-326637e281ce4cf7bfc6a4a4443c26cb/ai-cf92ca1c0ac24777a0a13ddc0f49a724/oidc/authorize',
        'https://test-20220617-idp.cig.tencentcs.com/sso/tn-326637e281ce4cf7bfc6a4a4443c26cb/ai-cf92ca1c0ac24777a0a13ddc0f49a724/oidc/token',
        'https://test-20220617-idp.cig.tencentcs.com/sso/tn-326637e281ce4cf7bfc6a4a4443c26cb/ai-cf92ca1c0ac24777a0a13ddc0f49a724/oidc/userinfo',
        0,
        '2022-06-17 14:10:46',
        '2022-06-17 14:10:46');