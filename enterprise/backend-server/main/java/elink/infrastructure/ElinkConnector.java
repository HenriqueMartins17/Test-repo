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

package com.apitable.enterprise.elink.infrastructure;

import java.io.UnsupportedEncodingException;
import com.apitable.enterprise.elink.infrastructure.ElinkTemplate.AgentApp;

/**
 * Elink Connector
 */
public interface ElinkConnector {

    /**
     * Get user information by code.
     * @param code
     * @return
     */
    String getUserIdByCode(AgentApp agentApp,String code);

    /**
     * Generate redirection login address through domain name
     * @param agentApp Request domain address
     * @param redirectUri
     * @return generate final UrL
     */
    String buildRedirectUrl(AgentApp agentApp,String redirectUri) throws UnsupportedEncodingException;

    /**
     * Generate qr login address through domain name
     * @param agentApp Request domain address
     * @param redirectUri
     * @return generate final UrL
     */
    String buildQRRedirectUrl(AgentApp agentApp, String redirectUri) throws UnsupportedEncodingException;

    /**
     * Obtain the associated AgentApp through host, if it does not exist,
     * * return the first AgentApp
     * @return Elinkapp
     */
    AgentApp getAgentAppByHost(String host);
}
