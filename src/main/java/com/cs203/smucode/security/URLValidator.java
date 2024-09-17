package com.cs203.smucode.security;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.MalformedURLException;
import java.net.URL;

public class URLValidator implements ConstraintValidator<ValidURL, String> {

    @Override
    public void initialize(ValidURL constraintAnnotation) {}

    @Override
    public boolean isValid(String url, ConstraintValidatorContext context) {
        if (url == null || url.isEmpty()) {
            return true; // Return true because we allow no profile pictures
        }

        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
