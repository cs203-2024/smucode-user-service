package com.cs203.smucode.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

@Configuration
@Profile("test")
public class TestSecurityConfiguration {

    @Bean
    public JwtDecoder jwtDecoder() {
        return token ->
            new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("scope", "read")
            );
    }
}
