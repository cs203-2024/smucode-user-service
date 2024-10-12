package com.cs203.smucode.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

/**
 * @author gav
 * @since 2024-10-12
 */
@Configuration
public class AWSConfig {

    @Value("${aws.role.arn}")
    private String roleArn;

    @Value("${aws.region}")
    private String awsRegion;
    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (roleArn != null && !roleArn.isEmpty()) {
            StsClient stsClient = StsClient.builder()
                    .region(Region.of(awsRegion))
                    .build();

            AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName("app-session")
                    .build();

            return StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(stsClient)
                    .refreshRequest(assumeRoleRequest)
                    .build();
        } else {
            return DefaultCredentialsProvider.create();
        }
    }
}