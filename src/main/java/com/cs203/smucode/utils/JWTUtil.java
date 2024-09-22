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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * @author: gav
 * @version: 1.1
 * @since: 24-09-18
 * @description: Utility class for JWT operations
 */
@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    @Value("${jwt.expiration}")
    private long expirationTime;

    private RSASSASigner rsaSigner;

    @PostConstruct
    public void init()
        throws InvalidKeySpecException, NoSuchAlgorithmException {

        String privateKeyStr = AWSUtil.getValueFromSecretsManager("JWTPrivateKey");
        logger.info("Fetched JWT private key: {}", privateKeyStr);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
            privateKeyBytes
        );
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        rsaSigner = new RSASSASigner(privateKey);
    }

    public String generateToken(Authentication auth) throws JOSEException {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();

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
