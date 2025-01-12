package com.apitable.appdata.shared.starter.oss;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AwsS3OssClientRequestFactory {

    private AWSCredentials credentials;

    private EndpointConfiguration configuration;

    private String bucketPolicy;

    public AwsS3OssClientRequestFactory(AWSCredentials credentials, EndpointConfiguration configuration, String bucketPolicy) {
        this.credentials = credentials;
        this.configuration = configuration;
        this.bucketPolicy = bucketPolicy;
    }

    public AwsOssClientRequest createClient() {
        AmazonS3 s3Client =
                AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(credentials))
                        .withEndpointConfiguration(configuration)
                        .enablePathStyleAccess()
                        .build();
        return new AwsOssClientRequest(s3Client, true, bucketPolicy);
    }
}
