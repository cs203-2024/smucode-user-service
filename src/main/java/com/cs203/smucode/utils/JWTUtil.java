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
* @description: Utility class for JWT operations
*/
@Component
public class JWTUtil {
   @Value("${jwt.secret}")
   private String jwtSecret;

   @Value("${jwt.expiration}")
   private long expiration; // 1 hour

}
