package com.cs203.smucode.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
* @author: gav
* @version: 1.0
* @since: 24-09-06
* @description: Utility class for JWT operations (generate, validate, get subject)
*/
@Component
public class JWTUtil {
   @Value("${jwt.secret}")
   private String jwtSecret;

   @Value("${jwt.expiration}")
   private long expiration; // 1 hour

    public String generateToken(String username) { 
        // Convert the secret key to a Key object -> cuz need key to cryptographically sign the JWT
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        // Build the JWT 
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(key) 
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public String getUsernameFromToken(String token) {
}
