package com.apitable.enterprise.license.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.binary.Base64;

public class LicenseUtil {

    public static boolean verifyData(String dataString, String signatureString)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        PublicKey publicKey = LicensePublicKeyLoader.getPublicKey();
        byte[] signatureBytes = Base64.decodeBase64(signatureString);
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(dataString.getBytes(StandardCharsets.UTF_8));
        return signature.verify(signatureBytes);
    }
}
