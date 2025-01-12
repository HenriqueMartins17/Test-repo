# APITable <https://github.com/apitable/apitable>
# Copyright (C) 2022 APITable Ltd. <https://apitable.com>
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

FROM liquibase/liquibase:4.21.1
RUN lpm add mysql --global
ADD src/main/resources /liquibase/changelog

CMD ["sh", "-c", "[ -z ${EDITION} ] && EDITION=apitable-saas; \
                    [ $EDITION = 'apitable-saas' ] && CHANGELOG_TABLE_SUFFIX=saas_db_changelog || CHANGELOG_TABLE_SUFFIX=enterprise_db_changelog; \
                    [ $EDITION = 'apitable-saas' ] && CHANGELOG_LOCK_TABLE_SUFFIX=saas_db_changelog_lock || CHANGELOG_LOCK_TABLE_SUFFIX=enterprise_db_changelog_lock; \
                    echo Execute Action: shared enterprise changelog; \
                    docker-entrypoint.sh \
                    --classpath=\"/liquibase/changelog/shared\" \
                    --changelog-file=\"db/changelog/db.changelog-master.xml\" \
                    --log-level=warning \
                    --username=${DB_USERNAME} \
                    --password=${DB_PASSWORD} \
                    --driver=\"com.mysql.cj.jdbc.Driver\" \
                    --database-changelog-table-name=\"${DATABASE_TABLE_PREFIX:=apitable_}shared_enterprise_db_changelog\" \
                    --database-changelog-lock-table-name=\"${DATABASE_TABLE_PREFIX:=apitable_}shared_enterprise_db_changelog_lock\" \
                    --url=\"jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?characterEncoding=utf8&autoReconnect=true&useSSL=true\" \
                    update -Dtable.prefix=${DATABASE_TABLE_PREFIX:=apitable_}; \
                    echo Execute Action: ${EDITION} enterprise changelog; \
                    docker-entrypoint.sh \
                    --classpath=\"/liquibase/changelog/${EDITION}\" \
                    --changelog-file=\"db/changelog/db.changelog-master.xml\" \
                    --log-level=warning \
                    --username=${DB_USERNAME} \
                    --password=${DB_PASSWORD} \
                    --driver=\"com.mysql.cj.jdbc.Driver\" \
                    --database-changelog-table-name=\"${DATABASE_TABLE_PREFIX:=apitable_}${CHANGELOG_TABLE_SUFFIX}\" \
                    --database-changelog-lock-table-name=\"${DATABASE_TABLE_PREFIX:=apitable_}${CHANGELOG_LOCK_TABLE_SUFFIX}\" \
                    --url=\"jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?characterEncoding=utf8&autoReconnect=true&useSSL=true\" \
                     ${ACTION:=update} \
                    -Dtable.prefix=${DATABASE_TABLE_PREFIX:=apitable_}"]



