package com.cs203.smucode.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UserIdentificationDTO(
        @NotNull(message = "Username cannot be null")
        @NotEmpty(message = "Username cannot be empty")
        String username
) {
}
