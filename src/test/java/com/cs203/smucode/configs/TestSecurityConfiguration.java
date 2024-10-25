@Configuration
@Profile("test")
public class TestConfig {

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
