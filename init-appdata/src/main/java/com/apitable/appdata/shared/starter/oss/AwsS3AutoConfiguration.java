package com.apitable.appdata.shared.starter.oss;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AmazonS3.class)
@ConditionalOnProperty(value = "starter.oss.enabled", havingValue = "true")
public class AwsS3AutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3AutoConfiguration.class);

    private final OssProperties properties;

    public AwsS3AutoConfiguration(OssProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(OssClientTemplate.class)
    public OssClientTemplate ossClientTemplate() {
        LOGGER.info("Oss starter autoconfiguration finish.");
        AWSCredentials credentials = new BasicAWSCredentials(properties.getAccessKeyId(), properties.getAccessKeySecret());
        EndpointConfiguration configuration =
                new AwsClientBuilder.EndpointConfiguration(properties.getEndpoint(), properties.getRegion());
        AwsS3OssClientRequestFactory ossClientRequestFactory = new AwsS3OssClientRequestFactory(credentials, configuration, properties.getBucketPolicy());
        OssClientTemplate template = new OssClientTemplate();
        template.setOssClientRequestFactory(ossClientRequestFactory);
        return template;
    }
}
