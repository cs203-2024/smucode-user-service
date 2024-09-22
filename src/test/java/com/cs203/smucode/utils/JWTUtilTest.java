package com.cs203.smucode.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JWTUtilTest {
    private JWTUtil jwtUtil;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private RSASSASigner signer;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        ReflectionTestUtils.setField(jwtUtil, "expirationTime", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "rsaSigner", signer);
    }

    @Test
    void generateToken_ShouldReturnValidJWT() throws JOSEException, ParseException {

        // Setup signer and auth object
        String username = "testuser";
        when(authentication.getName()).thenReturn(username);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(signer.supportedJWSAlgorithms()).thenReturn(Collections.singleton(JWSAlgorithm.RS256));
        when(signer.sign(any(JWSHeader.class), any(byte[].class))).thenReturn(Base64URL.encode(new byte[32])); // Mock signature
        // need add check for correct authority generation

        String token = jwtUtil.generateToken(authentication);


        assertNotNull(token);
        assertEquals(3, token.split("\\.").length); // Ensure it's a valid JWT format

        SignedJWT signedJWT = SignedJWT.parse(token);
        assertEquals(username, signedJWT.getJWTClaimsSet().getSubject());
        assertEquals("smucode-user-service", signedJWT.getJWTClaimsSet().getIssuer());
        assertNotNull(signedJWT.getJWTClaimsSet().getIssueTime());
        assertNotNull(signedJWT.getJWTClaimsSet().getExpirationTime());

        verify(signer, times(1)).sign(any(JWSHeader.class), any(byte[].class));
    }

    @Test
    void generateToken_ShouldThrowExceptionWhenSigningFails() throws JOSEException {
        // Setup signer and auth object
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(signer.supportedJWSAlgorithms()).thenReturn(Collections.singleton(JWSAlgorithm.RS256));
        when(signer.sign(any(JWSHeader.class), any(byte[].class))).thenThrow(new JOSEException("Signing failed"));

        assertThrows(JOSEException.class, () -> jwtUtil.generateToken(authentication));
        verify(signer, times(1)).sign(any(JWSHeader.class), any(byte[].class));
    }
}