package com.cs203.smucode.models;

public record JwtUserDTO(
        String message,
        UserDTO userDTO,
        String token
) {
}
