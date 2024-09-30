package com.cs203.smucode.constants;

public interface OAuth2Constants {
    interface KeyWord {
        String RESPONSE_TYPE = "response_type";
        String GRANT_TYPE = "grant_type";
        String CLIENT_ID = "client_id";
        String CLIENT_SECRET = "client_secret";
        String REDIRECT_URI = "redirect_uri";
        String SCOPE = "scope";
        String STATE = "state";
        String CODE = "code";
    }

    interface GrantType {
        String AUTHORIZATION_CODE = "authorization_code";
        String CLIENT_CREDENTIALS = "client_credentials";
        String IMPLICIT = "implicit";
        String PASSWORD = "password";
        String REFRESH_TOKEN = "refresh_token";
    }

    interface TokenType {
        String BEARER_TOKEN = "Bearer";
        String BASIC_TOKEN = "Basic";
    }
}
