package org.mskcc.cbio.oncokb.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import java.io.File;
import java.util.Optional;

/**
 * Created by Yifu Yao on 3/11/2021
 */
public class S3Utils {

    private static S3Utils instance = null;
    private static final String S3_ACCESS_KEY = "aws.s3.accessKey";
    private static final String S3_SECRET_KEY = "aws.s3.secretKey";
    private static final String S3_REGION = "aws.s3.region";

    public S3Utils() {}

    public static S3Utils getInstance() {
        if (instance == null) {
            instance = new S3Utils();
        }
        return instance;
    }

    public boolean isPropertiesConfigured() {
        if (
            PropertiesUtils.getProperties(S3_ACCESS_KEY) != null &&
            !PropertiesUtils.getProperties(S3_ACCESS_KEY).isEmpty() &&
            PropertiesUtils.getProperties(S3_SECRET_KEY) != null &&
            !PropertiesUtils.getProperties(S3_SECRET_KEY).isEmpty() &&
            PropertiesUtils.getProperties(S3_REGION) != null &&
            !PropertiesUtils.getProperties(S3_REGION).isEmpty()
        ) {
            return true;
        }
        return false;
    }

    private AmazonS3 getClient() {
        String s3AccessKey = PropertiesUtils.getProperties(S3_ACCESS_KEY);
        String s3SecretKey = PropertiesUtils.getProperties(S3_SECRET_KEY);
        String s3Region = PropertiesUtils.getProperties(S3_REGION);
        AWSCredentials credentials = new BasicAWSCredentials(
            s3AccessKey,
            s3SecretKey
        );
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(s3Region)
            .build();
    }

    /**
     * Save an object to aws s3
     * @param bucket s3 bucket name
     * @param objectPath the path where the object will be saved
     * @param file the object
     */
    public void saveObject(String bucket, String objectPath, File file) {
        getClient().putObject(bucket, objectPath, file);
    }

    /**
     * Get an object from aws s3
     * @param bucket s3 bucket name
     * @param objectPath the path of the object
     * @return a S3 object
     */
    public Optional<S3Object> getObject(String bucket, String objectPath) {
        try {
            S3Object s3object = getClient().getObject(bucket, objectPath);
            return Optional.of(s3object);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
