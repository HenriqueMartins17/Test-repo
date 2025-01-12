# Environment Variable

The following table lists all the server configuration properties.

## Backend Server Environment Variable Table

| Category    | Environment Variable Name    | Description                                                                                                                                        | Default Value                |
|-------------|------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------|
| Server      | API_DOCS_ENABLED             | Whether enable openapi document, access address is localhost:8081/api/v1/doc.html                                                                  | false                        |
| Server      | MAX_HTTP_HEADER_SIZE         | Max size of http request header                                                                                                                    | 32KB                         |
| Server      | MAX_FILE_SIZE                | Max upload size of http request                                                                                                                    | 1GB                          |
| Server      | MAX_REQUEST_SIZE             | Max size of http request                                                                                                                           | 1GB                          |
| Server      | SESSION_NAMESPACE            | Http cookies namespace key in cache                                                                                                                | apitable:session             |
| Server      | SESSION_TIMEOUT              | Http cookies survival time                                                                                                                         | 30d                          |
| Server      | DEFAULT_LOCALE               | Default server locale, support en-US/zh-CN                                                                                                         | en-US                        |
| Server      | SERVER_DOMAIN                | Server access address for all services, usually the ip address deployed by gateway services (like nginx,openresty, et), eg. http(s)://apitable.com | http://127.0.0.1             |
| Server      | ASSETS_LTD_URL               | The url of assets storage server like minio/s3 et, eg. http(s)://xxx.com                                                                           | http://127.0.0.1:9000/assets |
| Server      | ASSETS_LTD_BUCKET            | Bucket name of assets storage. keep default value if not necessary                                                                                 | assets                       |
| Database    | MYSQL_HOST                   | The host for the mysql database                                                                                                                    | 127.0.0.1                    |
| Database    | MYSQL_PORT                   | The port for the mysql database                                                                                                                    | 3306                         |
| Database    | MYSQL_DATABASE               | The db schema for the mysql database                                                                                                               | apitable                     |
| Database    | MYSQL_USERNAME               | The user name for the mysql database                                                                                                               | apitable                     |
| Database    | MYSQL_PASSWORD               | The password for the mysql database                                                                                                                | apitable@com                 |
| Redis Cache | REDIS_HOST                   | The host for redis cache                                                                                                                           | 127.0.0.1                    |
| Redis Cache | REDIS_USERNAME               | The username for redis cache                                                                                                                       | NULL                         |
| Redis Cache | REDIS_PASSWORD               | The password for redis cache                                                                                                                       | NULL                         |
| Redis Cache | REDIS_PORT                   | The port for redis cache                                                                                                                           | 6379                         |
| Redis Cache | REDIS_DB                     | The db index for redis cache                                                                                                                       | 0                            |
| MQ          | RABBITMQ_HOST                | The host for rabbitmq server                                                                                                                       | 127.0.0.1                    |
| MQ          | RABBITMQ_PORT                | The port for rabbitmq server                                                                                                                       | 5672                         |
| MQ          | RABBITMQ_USERNAME            | The username for rabbitmq server                                                                                                                   | apitable                     |
| MQ          | RABBITMQ_PASSWORD            | The password for rabbitmq server                                                                                                                   | apitable@com                 |
| MQ          | RABBITMQ_VHOST               | The vhost path for rabbitmq server                                                                                                                 | /                            |
| Mail        | EMAIL_PERSONAL               | Email sign personal show in email                                                                                                                  | APITable                     |
| Mail        | MAIL_HOST                    | The host for smtp server                                                                                                                           | NULL                         |
| Mail        | MAIL_USERNAME                | The username for smtp server, usually email address                                                                                                | NULL                         |
| Mail        | MAIL_PASSWORD                | The password for smtp server, usually email password                                                                                               | NULL                         |
| Mail        | MAIL_PORT                    | The port for smtp server, 465 for production, 25 for development                                                                                   | 465                          |
| Mail        | MAIL_SSL_ENABLE              | Whether mail ssl enable                                                                                                                            | true                         |
| Socket      | SOCKET_URL                   | Connect to room server for collaborating working by this properties, set this value in cluster mode, eg. http://room-server:3002                   | http://127.0.0.1:3002        |
| Socket      | SOCKET_RECONNECTION_ATTEMPTS | Set this value for attempting to reconnect socket times                                                                                            | 2                            |
| Socket      | SOCKET_RECONNECTION_DELAY    | Time delay to attempt reconnect socket, unit is seconds                                                                                            | 1000                         |
| Socket      | SOCKET_TIMEOUT               | Timeout to connect socket server, unit is seconds                                                                                                  | 1000                         |
| Storage     | OSS_ENABLED                  | Whether to enable storage, usually opened for uploading attachment in datasheet                                                                    | true                         |
| Storage     | OSS_CLIENT_TYPE              | Which client sdk type you want to use, aws s3 sdk is best, keep default value                                                                      | aws                          |
| Storage     | AWS_ACCESS_KEY               | Access key of AWS S3                                                                                                                               | NULL                         |
| Storage     | AWS_ACCESS_SECRET            | Access secret of AWS S3                                                                                                                            | NULL                         |
| Storage     | AWS_ENDPOINT                 | Endpoint of AWS S3                                                                                                                                 | NULL                         |
| Storage     | AWS_REGION                   | Region of AWS S3                                                                                                                                   | NULL                         |
| Storage     | QINIU_ACCESS_KEY             | Access key of QINIU Cloud                                                                                                                          | NULL                         |
| Storage     | QINIU_SECRET_KEY             | Secret key of QINIU Cloud                                                                                                                          | NULL                         |
| Storage     | QINIU_REGION                 | Region of QINIU Cloud                                                                                                                              | NULL                         |
| Storage     | QINIU_DOWNLOAD_DOMAIN        | Download domain of QINIU Cloud, optional                                                                                                           | NULL                         |
| Storage     | QINIU_UPLOAD_URL             | Upload of QINIU Cloud, optional                                                                                                                    | NULL                         |
| Storage     | QINIU_CALLBACK_URL           | Callback url of QINIU Cloud, optional                                                                                                              | NULL                         |
| Storage     | QINIU_CALLBACK_BODY_TYPE     | Callback body type of QINIU Cloud, optional                                                                                                        | NULL                         |
| Storage     | MINIO_ENDPOINT               | Endpoint of MinIO                                                                                                                                  | NULL                         |
| Storage     | MINIO_ACCESS_KEY             | Access key of MinIO                                                                                                                                | NULL                         |
| Storage     | MINIO_SECRET_KEY             | Secret key of MinIO                                                                                                                                | NULL                         |
| Report      | SENTRY_DSN                   | Sentry organization DSN url, set this value to open sentry bug report                                                                              | NULL                         |
| Report      | ENV                          | Sentry env, it depend on your environment, eg. development/test/production                                                                         | NULL                         |
| Connect     | SOCKET_DOMAIN                | The socket server address in cluster mode, eg. http://socket-server:3001/socket                                                                    | http://127.0.0.1:3001/socket |
| Connect     | NEST_GRPC_ADDRESS            | The room server grpc service, eg. static://room-server:3334                                                                                        | static://localhost:3334      |
| ---         | ---                          | ---                                                                                                                                                | ---                          |
| Auth        | AUTH0_ENABLED                | Whether enable auth0 connect, **only for enterprise**                                                                                              | false                        |
| Auth        | AUTH0_CLIENT_ID              | Auth0 tenant application client id, **only for enterprise**                                                                                        | NULL                         |
| Auth        | AUTH0_CLIENT_SECRET          | Auth0 tenant application client secret, **only for enterprise**                                                                                    | NULL                         |
| Auth        | AUTH0_CLIENT_DOMAIN          | Auth0 tenant client domain, **only for enterprise**                                                                                                | NULL                         |
| Auth        | AUTH0_CLIENT_ISSUER_URI      | Auth0 tenant client issuer url, **only for enterprise**                                                                                            | NULL                         |
| Auth        | AUTH0_REDIRECT_URI           | Auth0 tenant redirect uri, **only for enterprise**                                                                                                 | NULL                         |
| Auth        | AUTH0_DB_CONNECTION_ID       | Auth0 tenant db connect id, **only for enterprise**                                                                                                | NULL                         |
| Auth        | AUTH0_DB_CONNECTION_NAME     | Auth0 tenant db connect name, **only for enterprise**                                                                                              | NULL                         |

## Init-db Server Environment Variable Table

| Environment Variable Name | Description                          | Default Value | Require |
|---------------------------|--------------------------------------|---------------|---------|
| DB_HOST                   | the host of mysql database           | NULL          | true    |
| DB_PORT                   | the port of mysql database           | NULL          | true    |
| DB_NAME                   | the db schema name of mysql database | NULL          | true    |
| DB_USERNAME               | the username of mysql database       | NULL          | true    |
| DB_PASSWORD               | the password of mysql database       | NULL          | true    |
| DATABASE_TABLE_PREFIX     | table prefix                         | apitable_     | true    |
| ACTION                    | liquibase run action                 | update        | true    |

## Room Server Environment Variable Table

| Environment Variable Name   | Description                          | Default Value                                |
|-----------------------------|--------------------------------------|----------------------------------------------|
| NODE_ENV                    | Environment variable (development)   | development                                  |
| PORT                        | -                                    | 3333                                         |
| LOG_LEVEL                   | Logging level                        | debug                                        |
| DATABASE_TABLE_PREFIX       | -                                    | apitable_                                    |
| MYSQL_CONNECTION_LIMIT      | -                                    | 20                                           |
| MYSQL_DATABASE              | -                                    | apitable                                     |
| MYSQL_HOST                  | MySQL configuration                  | 127.0.0.1                                    |
| MYSQL_KEEP_CONNECTION_ALIVE | -                                    | true                                         |
| MYSQL_PASSWORD              | -                                    | apitable@com                                 |
| MYSQL_PORT                  | -                                    | 3306                                         |
| MYSQL_RETRY_DELAY           | (in ms)                              | 300                                          |
| MYSQL_USERNAME              | -                                    | root                                         |
| REDIS_DB                    | If not configured, default is 0      | 3                                            |
| REDIS_HOST                  | Redis configuration                  | 127.0.0.1                                    |
| REDIS_PASSWORD              | -                                    | apitable@com                                 |
| REDIS_PORT                  | -                                    | 6379                                         |
| SOCKET_GRPC_URL             | SOCKET-server configuration          | 0.0.0.0:3007                                 |
| BACKEND_BASE_URL            | Backend service address              | http://localhost:8081/api/v1/                |
| SERVER_MAX_FIELD_COUNT      | -                                    | 200                                          |
| SERVER_MAX_RECORD_COUNT     | -                                    | 50000                                        |
| SERVER_MAX_VIEW_COUNT       | -                                    | 30                                           |
| SERVER_RECORD_REMIND_RANGE  | -                                    | 90                                           |
| SERVER_TRANSFORM_LIMIT      | -                                    | 100000                                       |
| OSS_BUCKET                  | -                                    | QNY1                                         |
| OSS_CACHE_TYPE              | -                                    |                                              |
| OSS_HOST                    | OSS configuration                    | https://s1.vika.cn                           |
| OSS_MINIO                   | Minio                                | `{ "endPoint": "", "port": 9000, "useSSL": true, "accessKey": "", "secretKey": "" }` |
| OSS_MINIO_BUCKET_CACHE      | -                                    | `{ "name": "api.cache" }`                    |
| OSS_S3_BUCKET_CACHE         | S3                                   | `{ "name": "api.cache", "region": "cn-northwest-1" }` |
| ACTUATOR_DNS_URL            | Health check configuration           | http://health-check-monitor.vika.cn          |
| ACTUATOR_HEAP_RATIO         | -                                    | 100                                          |
| ACTUATOR_RSS_RATIO          | -                                    | 90                                           |
| LIMIT_DURATION              | -                                    | 1                                            |
| LIMIT_POINTS                | Current limit configuration          | 5                                            |
| LIMIT_WHITE_LIST            | -                                    | `{ "test1": { "points": 5, "duration": 1}, "test2": {"points": 5, "duration": 1 } }` |
| ENABLE_OTEL_JAEGER          | Enable tracking service (self-hosted)| false                                        |
| OTEL_JAEGER_ENDPOINT        | -                                    |                                              |
| OTEL_JAEGER_TRACE_ID_RATIO_BASED | Interval value (0 to 1)          | 0.1                                          |
| SENTRY_DSN                  | Sentry                               | https://14aceedcbea54ef8ac7ee4d70525f530@sentry.apitable.com/ |
| ENABLE_SOCKET               | enable socket module for collaboration |                                            |

## Web Server Environment Variable Table

| Category  | Environment Variable Name             | Description                              | Default Value         |
|-----------|---------------------------------------|------------------------------------------|-----------------------|
| Limit     | VIEW_NAME_MAX_COUNT                   | Maximum number of views for a datasheet  | 50                    |
| Limit     | MAXIMUM_VIEW_COUNT_PER_DATASHEET      | Maximum number of of widgets             | 30                    |
| Limit     | VIEW_NAME_MAX_COUNT                   | Maximum length of view name              | 50                    |
| UI        | FAVICON                               | favicon                                  | NULL                  |
| UI        | LOGIN_DEFAULT_ACCOUNT_TYPE            | Default login method                     | mail                  |
| UI        | ACCOUNT_LOGOUT_VISIBLE                | Allow login out                          | true                  |
| UI        | ACCOUNT_RESET_PASSWORD_VISIBLE        | Allow reset password                     | true                  |
| UI        | ROBOT_DEFAULT_AVATAR                  | Robot default avatar                     | NULL                  |
| UI        | REGENERATE_API_TOKEN_VISIBLE          | Recreate API Token Button visible or not | false                 |
| Theme     | SYSTEM_CONFIGURATION_DEFAULT_THEME    | Default theme                            | dark                  |
| Lang      | SYSTEM_CONFIGURATION_DEFAULT_LANGUAGE | Defaut language                          | en_US                 |
| Storage   | QNY1,QNY2,QNY3                        | OSS bucket or storage url                | assets                |
| Sentry    | SENTRY_CONFIG_DSN                     | Sentry DSN                               | NULL                  |
| Proxy     | API_PROXY                             |                                          | http://127.0.0.1:8081 |
| Proxy     | API_ROOM_SERVER                       |                                          | NULL                  |
| Proxy     | API_BACKEND_SERVER                    |                                          | NULL                  |
| Proxy     | API_FUSION_SERVER                     |                                          | NULL                  |
| Proxy     | API_SOCKET_SERVER_ROOM                |                                          | NULL                  |
| Setting   | NEXT_PUBLIC_ASSET_PREFIX              | Public static folder path                | ""                    |
| Setting   | USE_CUSTOM_PUBLIC_FILES               |                                          | NULL                  |
| Setting   | PUBLIC_URL                            |                                          | NULL                  |
| Analytics | GOOGLE_ANALYTICS_ID                   | Google Analytics ID                      | NULL                  |
| Analytics | BAIDU_ANALYSE_ID                      | Baidu Analytics ID                       | NULL                  |
| Security  | COOKIEBOT_ID                          | Cookiebot ID                             | Null                  |
