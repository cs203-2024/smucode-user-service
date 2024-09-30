package com.cs203.smucode.controllers;

import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.mappers.UserMapper;
import com.cs203.smucode.models.JwtUserDTO;
import com.cs203.smucode.models.User;
import com.cs203.smucode.models.UserDTO;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.util.Map;
import de.gesundkrank.jskills.Rating;

/**
 * @author: jere
 * @version: 1.2
 * @since: 2024-09-26
 */

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserController(IUserService userService, AuthenticationManager
            authenticationManager, JWTUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new ApiRequestException("Username cannot be null or empty");
            }

            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new ApiRequestException("User not found with username: " + username);
            }
            UserDTO userDTO = UserMapper.INSTANCE.userToUserDTO(user);

            return ResponseEntity.ok(userDTO);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user", e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtUserDTO> login(@RequestBody UserDTO userDTO) {
        try {
            String username = userDTO.username();
            String password = userDTO.password();
            if (username == null || password == null) {
                throw new ApiRequestException("Username and password are required");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.username(), userDTO.password())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            userDTO = UserMapper.INSTANCE.userToUserDTO(userService.getUserByUsername(username));

            return ResponseEntity.ok(new JwtUserDTO(
                    "success",
                    userDTO, jwtUtil.generateToken(authentication))
            );
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtUserDTO(
                            "Invalid username or password",
                            null, null)
                    );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new JwtUserDTO(
                            "Ensure that you have typed the username and password correctly",
                            null, null)
                    );
        } catch (ApiRequestException e) {
            logger.info(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            throw new ApiRequestException("An error occurred during login", e);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDTO> signup(@RequestBody UserDTO dto) {
        try {
            if (dto.username() == null || dto.password() == null) {
                throw new ApiRequestException("Username and password are required for signup");
            }
            User createdUser = UserMapper.INSTANCE.userDTOtoUser(dto);
            userService.createUser(createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred during signup", e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        //logout logic here?
        return ResponseEntity.ok("User logged out successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new ApiRequestException("User not found with username: " + username);
            }
            UserDTO userDTO = UserMapper.INSTANCE.userToUserDTO(user);
            return ResponseEntity.ok(userDTO);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user profile", e);
        }
    }

    //true skill-related
    @PutMapping("/{username}/update-rating")
    public ResponseEntity<String> updateRating(@PathVariable String username, @RequestBody Map<String, Object> ratingData) {
        try {
            //validate that both 'mu' and 'sigma' are present, and of valid type
            if (!ratingData.containsKey("mu") || !(ratingData.get("mu") instanceof Number)) {
                throw new ApiRequestException("'mu' is required and must be a valid number");
            }
            if (!ratingData.containsKey("sigma") || !(ratingData.get("sigma") instanceof Number)) {
                throw new ApiRequestException("'sigma' is required and must be a valid number");
            }

            //then we cast to double
            double mu = ((Number) ratingData.get("mu")).doubleValue();
            double sigma = ((Number) ratingData.get("sigma")).doubleValue();

            Rating newRating = new Rating(mu, sigma);
            userService.updateUserRating(username, newRating);

            return ResponseEntity.ok("User rating updated successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while updating the user's rating", e);
        }
    }
}