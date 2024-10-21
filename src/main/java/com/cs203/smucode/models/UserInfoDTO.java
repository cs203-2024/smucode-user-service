package com.cs203.smucode.models;

import com.cs203.smucode.security.ValidURL;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserInfoDTO (
    UUID id,

    @NotNull(message = "Username cannot be empty/null")
    String username,

    @NotNull(message = "Email cannot be empty/null")
    @Email(message = "Invalid email format")
    String email,

    @NotNull(message = "Image link cannot be null")
    @ValidURL
    String profileImageUrl,

    double mu,
    double sigma,
    double skillIndex
) {}
