package com.apitable.appdata.shared.starter.oss;

import java.io.IOException;
import java.io.InputStream;

public class OssClientTemplate {

    private AwsS3OssClientRequestFactory ossClientRequestFactory;

    public OssClientTemplate() {
    }

    public AwsS3OssClientRequestFactory getOssClientRequestFactory() {
        return ossClientRequestFactory;
    }

    public void setOssClientRequestFactory(AwsS3OssClientRequestFactory ossClientRequestFactory) {
        this.ossClientRequestFactory = ossClientRequestFactory;
    }

    public void upload(String bucketName, InputStream in, String keyPath) throws IOException {
        AwsOssClientRequest request = getOssClientRequestFactory().createClient();
        request.uploadStreamForObject(bucketName, in, keyPath);
    }

    public void upload(String bucketName, InputStream in, String path, String mimeType, String digest) throws IOException {
        AwsOssClientRequest request = getOssClientRequestFactory().createClient();
        request.uploadStreamForObject(bucketName, in, path, mimeType, digest);
    }

}
