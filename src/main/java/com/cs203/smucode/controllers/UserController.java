package com.cs203.smucode.controllers;

import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.models.User;
import com.cs203.smucode.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import de.gesundkrank.jskills.Rating;

/**
 * @author: jere
 * @version: 1.1
 * @since: 2024-09-07
 */

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        try {
            if (username == null || username.isEmpty()) {
                throw new ApiRequestException("Username cannot be null or empty");
            }

            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new ApiRequestException("User not found with username: " + username);
            }

            return ResponseEntity.ok(user);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user", e);
        }
    }

    @PostMapping("/login")
    //assuming no DTO for login deets
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            if (username == null || password == null) {
                throw new ApiRequestException("Username and password are required");
            }
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
            return ResponseEntity.ok("User logged in successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred during login", e);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody User newUser) {
        try {
            if (newUser.getUsername() == null || newUser.getPassword() == null) {
                throw new ApiRequestException("Username and password are required for signup");
            }
            User createdUser = userService.createUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
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
    public ResponseEntity<User> getProfile(@RequestParam String username) {
        try {
            User user = userService.getUserByUsername(username);
            if (user == null) {
                throw new ApiRequestException("User not found with username: " + username);
            }
            return ResponseEntity.ok(user);
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