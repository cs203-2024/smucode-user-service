package com.cs203.smucode.models;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRatingDTO(

        @NotNull(message = "Username cannot be empty/null")
        String username,

        double mu,
        double sigma,
        double skillIndex
) {}
