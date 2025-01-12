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

package com.apitable.enterprise.auth0.autoconfigure;

import cn.hutool.core.util.StrUtil;
import com.auth0.AuthenticationController;
import com.auth0.exception.PublicKeyProviderException;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.utils.tokens.IdTokenVerifier;
import com.auth0.utils.tokens.SignatureVerifier;
import java.security.interfaces.RSAPublicKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * auth0 auto configuration.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Auth0Properties.class)
@ConditionalOnClass(AuthenticationController.class)
@ConditionalOnProperty(value = "auth0.enabled", havingValue = "true")
public class Auth0AutoConfiguration {

    private final Auth0Properties properties;

    public Auth0AutoConfiguration(Auth0Properties properties) {
        this.properties = properties;
    }

    /**
     * init auth0Template bean.
     *
     * @return Auth0Template
     */
    @Bean
    public Auth0Template auth0Template() {
        String domain = properties.getDomain();
        String audience = properties.getIssuerUri();
        String clientId = properties.getClientId();
        String clientSecret = properties.getClientSecret();
        String redirectUri = properties.getRedirectUri();
        String dbConnectionName = properties.getDbConnectionName();
        JwkProvider provider = new JwkProviderBuilder(domain).build();
        SignatureVerifier signatureVerifier = SignatureVerifier.forRS256(keyId -> {
            try {
                return (RSAPublicKey) provider.get(keyId).getPublicKey();
            } catch (JwkException jwke) {
                throw new PublicKeyProviderException("Error obtaining public key", jwke);
            }
        });
        IdTokenVerifier idTokenVerifier =
            IdTokenVerifier.init(StrUtil.addSuffixIfNot(domain, "/"), clientId, signatureVerifier)
                .build();
        return new Auth0Template(domain, clientId, clientSecret, audience, redirectUri,
            dbConnectionName, idTokenVerifier);
    }
}
