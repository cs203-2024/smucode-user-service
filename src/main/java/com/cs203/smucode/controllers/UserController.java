package com.cs203.smucode.controllers;

import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.mappers.UserProfileMapper;
import com.cs203.smucode.dto.UserIdentificationDTO;
import com.cs203.smucode.dto.UserInfoDTO;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.dto.UserRatingDTO;
import com.cs203.smucode.services.IUserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import de.gesundkrank.jskills.Rating;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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


    @GetMapping("/profile/{username}")
    public ResponseEntity<UserInfoDTO> getProfile(@PathVariable String username) {
        try {
            validateUsername(username);

            UserProfile user = userService.getUserProfileByUsername(username);
            UserInfoDTO userInfoDTO = UserProfileMapper.INSTANCE.userProfileToUserInfoDTO(user);

            return ResponseEntity.ok(userInfoDTO);
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user profile");
        }
    }

    @PostMapping("/profile/create")
    public ResponseEntity<String> createUser(@RequestBody UserIdentificationDTO userIdentificationDTO) {
        try {
            validateUsername(userIdentificationDTO.username());

            UserProfile userProfile = UserProfileMapper.INSTANCE.userIdentificationDTOtoUserProfile(userIdentificationDTO);

            userService.createUserProfile(userProfile);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()         // Start from the base URL
                    .path("/api/users/profile")       // Add the base path including "profile"
                    .path("/{username}")              // Append the username parameter
                    .buildAndExpand(userProfile.getUsername())  // Insert the username into the path
                    .toUri();

            return ResponseEntity.created(location)
                    .body("Created user profile '" + userProfile + "'.");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user profile");
        }
    }

    //true skill-related
    @PutMapping("/update-rating")
    public ResponseEntity<String> updateRating(@RequestBody @Valid UserRatingDTO ratingDTO) {
        try {
            validateUsername(ratingDTO.username());

            Rating newRating = new Rating(ratingDTO.mu(), ratingDTO.sigma());
            userService.updateUserRating(ratingDTO.username(), newRating);

            return ResponseEntity.ok("User rating updated successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while updating the user's rating");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new ApiRequestException("Username cannot be null or empty");
        }
    }
}