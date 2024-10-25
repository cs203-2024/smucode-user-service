package com.cs203.smucode.dto;

import jakarta.validation.constraints.NotNull;

public record UserRatingDTO(

        @NotNull(message = "Username cannot be empty/null")
        String username,

        double mu,
        double sigma,
        double skillIndex
) {}
