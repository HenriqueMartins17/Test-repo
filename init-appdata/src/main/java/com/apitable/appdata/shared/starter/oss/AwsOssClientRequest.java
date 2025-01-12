package com.apitable.appdata.shared.starter.oss;

import java.io.InputStream;

import cn.hutool.core.util.StrUtil;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkBaseException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetBucketLocationRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsOssClientRequest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsOssClientRequest.class);

    private static final String CACHE_CONTROL_VALUE = "max-age=2592000,must-revalidate";

    private final AmazonS3 amazonClient;

    private boolean autoCreateBucket = false;

    private String bucketPolicy;

    public AwsOssClientRequest(AmazonS3 amazonClient) {
        this.amazonClient = amazonClient;
    }

    public AwsOssClientRequest(AmazonS3 amazonClient, boolean autoCreateBucket, String bucketPolicy) {
        this.amazonClient = amazonClient;
        this.autoCreateBucket = autoCreateBucket;
        this.bucketPolicy = bucketPolicy;
    }

    protected boolean isBucketExist(String bucketName) {
        boolean existBucket = amazonClient.doesBucketExistV2(bucketName);
        if (!existBucket) {
            if (autoCreateBucket) {
                amazonClient.createBucket(new CreateBucketRequest(bucketName));
                LOGGER.info("Auto create bucket[{}].", bucketName);
                if (bucketPolicy != null) {
                    amazonClient.setBucketPolicy(bucketName, bucketPolicy);
                    LOGGER.info("Setting bucket policy: {}.", bucketPolicy);
                }
                // verify that the bucket was created by retrieving it and checking its location.
                amazonClient.getBucketLocation(new GetBucketLocationRequest(bucketName));
            }
            else {
                throw new UnsupportedOperationException("Your bucket does not exist and cannot be initialized");
            }
        }
        return existBucket;
    }

    public void uploadStreamForObject(String bucketName, InputStream in, String keyPath) {
        uploadStreamForObject(bucketName, in, keyPath, null, null);
    }

    public void uploadStreamForObject(String bucketName, InputStream in, String path, String mimeType, String digest) {
        isBucketExist(bucketName);
        // use high-level staged upload
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(amazonClient)
                // Set the minimum partition size, which is 5MB by default. If the setting is too small, it will cause too many slices and affect the upload speed.
                .withMinimumUploadPartSize(10 * 1024 * 1024L)
                // Set the threshold for fragment upload. Only when the file is greater than this value will the file be uploaded in fragments,
                // otherwise the file will be uploaded in a normal way. The default value is 16MB.
                .withMultipartUploadThreshold(100 * 1024 * 1024L)
                .build();
        ObjectMetadata metadata = new ObjectMetadata();
        if (StrUtil.isNotBlank(mimeType)) {
            metadata.setContentType(mimeType);
        }
        if (StrUtil.isNotBlank(digest)) {
            metadata.setContentMD5(digest);
        }
        metadata.setCacheControl(CACHE_CONTROL_VALUE);
        try {
            Upload upload = tm.upload(bucketName, path, in, metadata);
            LOGGER.info("upload start......");
            upload.waitForCompletion();
            LOGGER.info("upload completed......");
        }
        catch (SdkBaseException e) {
            catchAwsBaseError(e);
        }
        catch (InterruptedException e) {
            // The upload process was interrupted
            e.printStackTrace();
            LOGGER.error("upload interrupted", e);
            throw new RuntimeException("upload interrupted", e);
        }
        finally {
            // interrupt upload
            LOGGER.info("end of upload");
            tm.shutdownNow();
        }
    }

    private void catchAwsBaseError(SdkBaseException e) {
        if (e instanceof AmazonServiceException) {
            // The transmission succeeds, but the S3 service cannot process it. An error is returned
            LOGGER.error("Transfer succeeded, storage service error", e);
            throw new RuntimeException("Transfer succeeded, storage service error", e);
        }
        else if (e instanceof AmazonClientException) {
            // The s3 service cannot be connected, or the client cannot parse the results returned by the s3 service
            LOGGER.error("Failed to upload client", e);
            throw new RuntimeException("Failed to upload client", e);
        }
    }
}
