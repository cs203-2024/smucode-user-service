package com.cs203.smucode.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * @author: gav
 * @version: 1.0
 * @since: 24-09-06
 * @description: Utility class for AWS services
 */
@Component
public class AWSUtil {

    public static String getValueFromSecretsManager(String secretName) {
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
            throw new RuntimeException("Error occurred with AWS", e);
        } catch (Exception e) {
            throw new RuntimeException("Error occurred when fetching secret", e);
        }
    }
}
