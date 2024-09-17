package com.cs203.smucode.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDTO(

        @NotNull(message = "Username cannot be null")
        String username,

        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be more than 8 characters")
        @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$")
        String password,

        @NotNull(message = "Role cannot be null")
        String role

) {}
