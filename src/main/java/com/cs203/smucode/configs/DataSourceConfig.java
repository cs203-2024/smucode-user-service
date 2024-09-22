package com.cs203.smucode.configs;

import com.cs203.smucode.utils.AWSUtil;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBInstance;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbInstancesResponse;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!test")
public class DataSourceConfig {
    // AWS-related configuration properties
    @Value("${aws.rds.instance.identifier}")
    private String dbInstanceIdentifier; // Identifier for the RDS instance

    @Value("${aws.region}")
    private String awsRegion; // AWS region where the RDS instance is located

    @Value("${spring.datasource.username}")
    private String dbUsername; // Database username

    @Value("${aws.secretsmanager.db.password.secret}")
    private String dbPasswordSecretName; // Name of the secret in AWS Secrets Manager that stores the database password
    
    @Bean
    public DataSource dataSource() {
        // Create an RDS client to interact with AWS RDS service
        RdsClient rdsClient = RdsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create()) // Use default AWS credentials provider chain
                .region(Region.of(awsRegion)) // Set the AWS region
                .build();

        // Create a request to describe the RDS instance
        DescribeDbInstancesRequest request = DescribeDbInstancesRequest.builder()
                .dbInstanceIdentifier(dbInstanceIdentifier) // Specify which RDS instance to describe
                .build();
        
        // Send the request and get the response
        DescribeDbInstancesResponse response = rdsClient.describeDBInstances(request);
        DBInstance dbInstance = response.dbInstances().get(0); // Get the first (and should be only) instance described

        // Construct the JDBC URL using the RDS instance details
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s",
                dbInstance.endpoint().address(), // RDS instance hostname
                dbInstance.endpoint().port(), // RDS instance port
                dbInstance.dbName()); // Database name

        // Retrieve the database password from AWS Secrets Manager
        String dbPassword = AWSUtil.getValueFromSecretsManager(dbPasswordSecretName);

        // Configure HikariCP connection pool
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver"); 
        
        // Create and return a HikariDataSource with the configured properties; repository will use this to connect to the database
        return new HikariDataSource(config);
    }
}
