/*
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

package com.apitable.enterprise.social.properties;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.apitable.enterprise.social.properties.FeishuAppProperties.PREFIX;

/**
 * feishu app properties
 * @author Shawn Deng
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class FeishuAppProperties {

    public static final String PREFIX = "feishu.app";

    private String adminUri;

    private String errorUri;

    private Boolean v2Enable = Boolean.FALSE;

    private String helpUri;

    private String helpDeskUri;
}
