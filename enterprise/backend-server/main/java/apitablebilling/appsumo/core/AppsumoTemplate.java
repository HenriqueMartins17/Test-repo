package com.apitable.enterprise.apitablebilling.appsumo.core;

import cn.hutool.core.util.StrUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.StringUtil;

/**
 * Appsumo template.
 */
@Slf4j
public class AppsumoTemplate {
    private final String appId;

    private final String appSecret;

    private static final String JWT_SUBJECT = "appsumo";

    public AppsumoTemplate(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }

    /**
     * generate token for appsumo notification auth.
     *
     * @return JWT token String
     */
    public String generateToken() {
        try {
            JwtClaims claims = new JwtClaims();
            claims.setAudience(this.appId);
            claims.setExpirationTimeMinutesInTheFuture(10);
            claims.setGeneratedJwtId(32);
            claims.setIssuedAtToNow();
            claims.setAudience(JWT_SUBJECT);
            claims.setSubject(JWT_SUBJECT);
            JsonWebSignature jws = new JsonWebSignature();
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(getKey(), "HmacSHA256");
            jws.setKey(key);
            // The payload of the JWS is JSON content of the JWT Claims
            jws.setPayload(claims.toJson());
            return jws.getCompactSerialization();
        } catch (Exception e) {
            log.error("generate json web token error", e);
        }
        return null;
    }

    /**
     * verify token.
     *
     * @param token jwt token
     * @return boolean
     */
    public boolean verifyToken(String token) {
        try {
            SecretKeySpec key = new SecretKeySpec(getKey(), "HmacSHA256");
            JwtConsumer jwtConsumer =
                new JwtConsumerBuilder()
                    .setRequireIssuedAt()
                    .setRequireExpirationTime()
                    .setExpectedSubject(JWT_SUBJECT)
                    .setExpectedAudience(JWT_SUBJECT)
                    // set the verification key
                    .setVerificationKey(key).build();
            jwtConsumer.process(token);
            return true;
        } catch (Exception e) {
            log.error("validate jwt error", e);
        }
        return false;
    }

    public boolean isUserMatch(String userName, String password) {
        return StrUtil.equals(userName, this.appId) && StrUtil.equals(this.appSecret, password);
    }

    private byte[] getKey() throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(StringUtil.getBytesUtf8(this.appSecret));
        return md.digest();
    }
}
