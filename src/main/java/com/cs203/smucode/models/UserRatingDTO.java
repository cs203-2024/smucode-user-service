package com.cs203.smucode.models;

import java.util.UUID;

public record UserRatingDTO(

        UUID id,

        double mu,
        double sigma,
        double skillIndex
) {}
