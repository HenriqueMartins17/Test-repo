-- Delete redundant fields --
alter table ${table.prefix}wechat_auth_permission drop create_time, drop update_time;

alter table ${table.prefix}wechat_authorization drop create_time, drop update_time;

alter table ${table.prefix}wechat_member drop create_time, drop update_time;
