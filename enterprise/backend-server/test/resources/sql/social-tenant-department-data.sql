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

INSERT INTO `social_tenant_department` (`id`, `tenant_id`, `space_id`,
                                                        `department_id`,
                                                        `open_department_id`, `parent_id`,
                                                        `parent_open_department_id`,
                                                        `department_name`,
                                                        `department_order`)
VALUES (41, 'ww41', 'spc41', 'di41', 'odi41', 'pdi45', 'podi45', 'Engineering Department', 0);