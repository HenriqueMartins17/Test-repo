ALTER table `${table.prefix}wechat_auth_permission`
    ADD COLUMN `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time';
ALTER table `${table.prefix}wechat_auth_permission`
    ADD COLUMN `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time';
UPDATE `${table.prefix}wechat_auth_permission` SET created_at = create_time, updated_at = update_time;

ALTER table `${table.prefix}wechat_authorization`
    ADD COLUMN `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time';
ALTER table `${table.prefix}wechat_authorization`
    ADD COLUMN `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time';
UPDATE `${table.prefix}wechat_authorization` SET created_at = create_time, updated_at = update_time;

ALTER table `${table.prefix}wechat_member`
    ADD COLUMN `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time';
ALTER table `${table.prefix}wechat_member`
    ADD COLUMN `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time';
UPDATE `${table.prefix}wechat_member` SET created_at = create_time, updated_at = update_time;
