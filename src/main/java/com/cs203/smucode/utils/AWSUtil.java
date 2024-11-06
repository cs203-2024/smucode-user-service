package com.cs203.smucode.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: gav
 * @version: 1.0
 * @since: 24-09-06
 * @description: Utility class for AWS services
 */
@Component
public class AWSUtil {

    private static final Logger logger = LoggerFactory.getLogger(AWSUtil.class);

    @Value("${aws.bucket.name}")
    private String bucketName;

    private final Map<String, String> usernameToKeyMap = new HashMap<>();

    public String getValueFromSecretsManager(String secretName) {
        try (
            SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.AP_SOUTHEAST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        ) {
            GetSecretValueRequest secretValueRequest =
                    GetSecretValueRequest.builder().secretId(secretName).build();

            GetSecretValueResponse secretValueResponse =
                    client.getSecretValue(secretValueRequest);
            return secretValueResponse.secretString();
        } catch (SdkException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException("Error occurred with AWS");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException("Error occurred when fetching secret");
        }
    }

    public String generatePresignedUrl(String username, String contentType) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build()) {

            String key = String.format("profile-picture/%s-%s", username, UUID.randomUUID());
            this.usernameToKeyMap.put(username, key);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(3))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

            return presignedRequest.url().toString();
        } catch (SdkException e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException("Error with AWS");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException("Error generating presigned URL");
        }
    }

    public String getObjectUrl(String username) {
        if (usernameToKeyMap.get(username) == null) {
            throw new IllegalStateException("Username " + username + " does not have an existing image");
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, Region.AP_SOUTHEAST_1.toString(), usernameToKeyMap.get(username));
    }
}
