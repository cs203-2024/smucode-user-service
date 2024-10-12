package com.cs203.smucode.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

/**
 * @author: gav
 * @version: 1.1
 * @since: 24-09-18
 * @description: Utility class for JWT operations
 */
@Component
public class JWTUtil {

    @Value("${jwt.expiration}")
    private long expirationTime;

    /**
     * RSA signer used to sign the JWT tokens.
     */
    private final RSASSASigner rsaSigner;

    @Autowired
    public JWTUtil(RSASSASigner rsaSigner) {
        this.rsaSigner = rsaSigner;
    }

    /**
     * Generates a JWT token post-authentication.
     *
     * @param auth the Authentication object containing user details
     * @return a String representation of the signed JWT token (for Bearer)
     */
    public String generateToken(Authentication auth) throws JOSEException, AuthenticationException {
        if (!(auth.getPrincipal() instanceof UserDetails userDetails)) {
            throw new AuthenticationServiceException("UserDetails not found, got " + auth.getPrincipal().getClass().getName());
        }

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
            .subject(auth.getName())
            .issuer("smucode-user-service")
            .issueTime(new Date())
            .expirationTime(
                new Date(System.currentTimeMillis() + expirationTime)
            )
            .claim(
                "roles",
                userDetails
                    .getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList())
            )
            .build();

        SignedJWT signedJWT = new SignedJWT(
            new JWSHeader(JWSAlgorithm.RS256),
            claimsSet
        );

        signedJWT.sign(rsaSigner);

        return signedJWT.serialize();
    }
}
