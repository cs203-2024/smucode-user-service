package com.cs203.smucode.models;

public record JwtUserDTO(
        UserDTO userDTO,
        String token
) {
}
