package com.cs203.smucode.controllers;

import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.mappers.UserMapper;
import com.cs203.smucode.models.User;
import com.cs203.smucode.models.UserDTO;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.services.impl.AuthUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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
    private final IUserService userService;
    private final UserDetailsService authUserService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserController(IUserService userService, AuthUserServiceImpl authUserService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authUserService = authUserService;
        this.passwordEncoder = passwordEncoder;
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
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try {
            String username = userDTO.username();
            String password = userDTO.password();
            if (username == null || password == null) {
                throw new ApiRequestException("Username and password are required");
            }
            UserDetails userDetails = authUserService.loadUserByUsername(username);
            if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            return ResponseEntity.ok("User logged in successfully");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
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