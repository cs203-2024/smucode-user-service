package com.cs203.smucode.controllers;

import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.mappers.UserMapper;
import com.cs203.smucode.models.User;
import com.cs203.smucode.models.UserDTO;
import com.cs203.smucode.models.UserInfoDTO;
import com.cs203.smucode.services.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserInfoDTO> getUser(@PathVariable String username) {
        try {
            validateUsername(username);

            User user = userService.getUserByUsername(username);
            UserInfoDTO userInfoDTO = UserMapper.INSTANCE.userToUserDTO(user);

            return ResponseEntity.ok(userInfoDTO);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user", e);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfoDTO> getProfile(@RequestParam String username) {
        try {
            validateUsername(username);
            User user = userService.getUserByUsername(username);
            UserInfoDTO userInfoDTO = UserMapper.INSTANCE.userToUserDTO(user);
            return ResponseEntity.ok(userInfoDTO);
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

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new ApiRequestException("Username cannot be null or empty");
        }
    }
}