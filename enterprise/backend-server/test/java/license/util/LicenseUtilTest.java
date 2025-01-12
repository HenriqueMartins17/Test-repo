package com.apitable.enterprise.license.util;


import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

public class LicenseUtilTest {

    @Test
    public void testVerifyData() throws NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException, UnsupportedEncodingException {
       boolean verify =  LicenseUtil.verifyData("apitable",
        "W64B0nB76qy9yYaC9inaJpLT+84fKw24NgtB+I3q0S+u9AKAO7CksoXTBHooy0emlAMg6240MbVsg/WAadypD+clxcq/0JmEFbXopPOgIN7xjgAIoQmQ01TASsZT2nmaY599tN2juGWnX6ywEsGPQX3PI51KpTrA/WgKQnD3V7I=");
       assertThat(verify).isTrue();
    }
}