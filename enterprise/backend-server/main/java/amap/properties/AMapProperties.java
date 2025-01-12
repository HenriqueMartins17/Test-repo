/*
 * APITable Ltd. <legal@apitable.com>
 * Copyright (C)  2022 APITable Ltd. <https://apitable.com>
 *
 * This code file is part of APITable Enterprise Edition.
 *
 * It is subject to the APITable Commercial License and conditional on having a fully paid-up license from APITable.
 *
 * Access to this code file or other code files in this `enterprise` directory and its subdirectories does not constitute permission to use this code or APITable Enterprise Edition features.
 *
 * Unless otherwise noted, all files Copyright © 2022 APITable Ltd.
 *
 * For purchase of APITable Enterprise Edition license, please contact <sales@apitable.com>.
 */

package com.apitable.enterprise.amap.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 
* <p> 
* amap properties
* </p> 
* @author zoe zheng 
*/
@ConfigurationProperties(prefix = "lbs.amap")
public class AMapProperties {

    private String key;

    private String jscode;

    private Proxy styles;

    private Proxy vectormap;

    private Proxy restapi;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getJscode() {
        return jscode;
    }

    public void setJscode(String jscode) {
        this.jscode = jscode;
    }

    public Proxy getStyles() {
        return styles;
    }

    public void setStyles(Proxy styles) {
        this.styles = styles;
    }

    public Proxy getVectormap() {
        return vectormap;
    }

    public void setVectormap(Proxy vectormap) {
        this.vectormap = vectormap;
    }

    public Proxy getRestapi() {
        return restapi;
    }

    public void setRestapi(Proxy restapi) {
        this.restapi = restapi;
    }

    public static class Proxy {

        private String proxyPass;

        public String getProxyPass() {
            return proxyPass;
        }

        public void setProxyPass(String proxyPass) {
            this.proxyPass = proxyPass;
        }
    }
}
