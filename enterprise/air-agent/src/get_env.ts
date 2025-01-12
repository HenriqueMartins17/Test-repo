/**
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Get system environment variables
 */
export const getEnvVars = () => {
  return {
    AI_ENTRANCE_VISIBLE: process.env.AI_ENTRANCE_VISIBLE === 'true',
    AI_OPEN_FORM: process.env.AI_OPEN_FORM,
    AUTH0_ENABLED: process.env.AUTH0_ENABLED === 'true',
    AI_API_HELP_URL: process.env.AI_API_HELP_URL,
    SYSTEM_CONFIGURATION_DEFAULT_LANGUAGE: process.env.SYSTEM_CONFIGURATION_DEFAULT_LANGUAGE,
    SYSTEM_CONFIGURATION_DEFAULT_THEME: process.env.SYSTEM_CONFIGURATION_DEFAULT_THEME,
    QNY1: process.env.QNY1,
    ONBOARDING_CUSTOMER_SERVICE_QRCODE_AVATAR_IMG: process.env.ONBOARDING_CUSTOMER_SERVICE_QRCODE_AVATAR_IMG,
  };
};
