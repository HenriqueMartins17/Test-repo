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

package com.apitable.enterprise.teg.autoconfigure;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Tencent teg.
 */
@ConfigurationProperties(prefix = "teg")
public class TegProperties {

    private boolean enabled = false;

    /** Token key from teg. */
    private String tokenKey;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getTokenKey() {
        return tokenKey;
    }

    public void setTokenKey(final String tokenKey) {
        this.tokenKey = tokenKey;
    }

    public List<String> getIgnoreUrls() {
        return ignoreUrls;
    }

    public void setIgnoreUrls(final List<String> ignoreUrls) {
        this.ignoreUrls = ignoreUrls;
    }

    /**
     * ignore Urls.
     */
    private List<String> ignoreUrls;

    public List<String> getCheckUrls() {
        return checkUrls;
    }

    public void setCheckUrls(final List<String> checkUrls) {
        this.checkUrls = checkUrls;
    }

    /**
     * Force check Urls.
     */
    private List<String> checkUrls;

    /**
     * Tencent teg's jwt request header.
     */
    public static class SmartProxyHeaderProperty {
        public static final String REQUEST_TIMESTAMP = "timestamp";
        public static final String REQUEST_SIGNATURE = "Signature";
        public static final String REQUEST_STAFFID = "Staffid";
        public static final String REQUEST_STAFFNAME = "Staffname";
        public static final String REQUEST_X_EXT_DATA = "x-ext-data";
        public static final String REQUEST_X_RIO_SEQ = "X-Rio-Seq";
        //internal header
        public static final String REQUEST_IGNORE_URL = "ignoreUrl";
        public static final String REQUEST_CHECK_URL = "checkUrl";
    }
}