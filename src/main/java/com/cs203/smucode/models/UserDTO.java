package com.cs203.smucode.models;

import com.cs203.smucode.security.ValidURL;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record UserDTO(

        @NotNull(message = "Username cannot be empty/null")
        String username,

        @NotNull(message = "Password cannot be empty/null")
        @Size(min = 8, message = "Password must be more than 8 characters")
        @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]*$")
        String password,

        @NotNull(message = "Email cannot be empty/null")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Image link cannot be null")
        @ValidURL
        String profileImageUrl,

        @NotNull(message = "Role cannot be empty/null")
        String role,

        Double mu,
        Double sigma,
        Double skillIndex

) {}
