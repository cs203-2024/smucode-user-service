package com.cs203.smucode.configs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("local")
public class LocalDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername; // Database username

    @Value("${spring.datasource.password}")
    private String dbPassword; // Name of the secret in AWS Secrets Manager that stores the database password

    @Bean
    public DataSource dataSource() {
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
