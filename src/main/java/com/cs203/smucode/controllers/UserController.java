package com.cs203.smucode.controllers;

import com.cs203.smucode.constants.MediaConstants;
import com.cs203.smucode.constants.OAuth2Constants;
import com.cs203.smucode.dto.*;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.exception.InvalidTokenException;
import com.cs203.smucode.mappers.UserProfileMapper;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.utils.AWSUtil;
import com.nimbusds.jwt.proc.BadJWTException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final AWSUtil awsUtil;

    @Autowired
    public UserController(IUserService userService, AWSUtil awsUtil) {
        this.userService = userService;
        this.awsUtil = awsUtil;
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
    public ResponseEntity<String> createUser(@RequestBody @Valid UserIdentificationDTO userIdentificationDTO) {
        try {

            UserProfile userProfile = UserProfileMapper.INSTANCE.userIdentificationDTOtoUserProfile(userIdentificationDTO);

            logger.info("Creating user profile {}", userProfile);
            userService.createUserProfile(userProfile);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentContextPath()         // Start from the base URL
                    .path("/api/users/profile")       // Add the base path including "profile"
                    .path("/{username}")              // Append the username parameter
                    .buildAndExpand(userProfile.getUsername())  // Insert the username into the path
                    .toUri();

            logger.info("Creating uri {}", location);

            return ResponseEntity.created(location)
                    .body("Created user profile '" + userProfile + "'.");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while fetching the user profile");
        }
    }

    @PostMapping("/profile/delete")
    public ResponseEntity<String> deleteUser(@RequestBody @Valid UserIdentificationDTO userIdentificationDTO) {
        try {
            UserProfile userProfile = userService.getUserProfileByUsername(userIdentificationDTO.username());
            userService.deleteUserProfile(userProfile.getId());

            return ResponseEntity.ok("Deleted user profile '" + userIdentificationDTO.username() + "'.");
        } catch (UsernameNotFoundException e) {
            throw new ApiRequestException("Username not found");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while deleting the user profile");
        }
    }

    @PostMapping("/get-upload-link")
    public ResponseEntity<UploadLinkResponseDTO> getPreSignedUrl(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String contentType
    ) {
        validateJwt(jwt);

        if (contentType == null || contentType.isEmpty()) {
            throw new ApiRequestException("Content type is mandatory");
        }

        if (!MediaConstants.SUPPORTED_MEDIA.contains(contentType)) {
            throw new ApiRequestException("Unsupported content type: " + contentType);
        }

        try {
            String username = jwt.getClaimAsString(OAuth2Constants.SUBJECT);
            String preSignedUrl = awsUtil.generatePresignedUrl(username, contentType);
            String key = awsUtil.getKey(username);

            logger.info("Making presigned link for {}", username);
            return ResponseEntity.ok(
                    new UploadLinkResponseDTO(key, preSignedUrl)
            );
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while uploading the profile picture");
        }
    }

    @PostMapping("/upload-picture")
    public ResponseEntity<UploadSuccessResponseDTO> uploadPicture(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam String key) {
        try {
            validateJwt(jwt);
            if (key == null || key.isEmpty()) {
                throw new ApiRequestException("Please provide a non-empty or null key");
            }

            if (!key.startsWith("user-pictures/")) {
                throw new ApiRequestException("Invalid key: " + key + ", please provide a valid key");
            }

            String username = jwt.getClaimAsString(OAuth2Constants.SUBJECT);

            if (!awsUtil.getKey(username).equals(key)) {
                throw new ApiRequestException("Input key does not match generated key");
            }


            logger.info("Making image link for {}", username);
            String imageUrl = awsUtil.getObjectUrl(username);
            userService.uploadProfilePicture(username, imageUrl);

            return ResponseEntity.ok(
                    new UploadSuccessResponseDTO("success", imageUrl)
            );
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while uploading the profile picture");
        }
    }

    //true skill-related
    @PutMapping("/update-rating")
    public ResponseEntity<String> updateRating(@RequestBody @Valid UserRatingDTO ratingDTO) {
        try {

            Rating newRating = new Rating(ratingDTO.mu(), ratingDTO.sigma());
            userService.updateUserRating(ratingDTO.username(), newRating);

            logger.info("Updating rating");

            return ResponseEntity.ok("User rating updated successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while updating the user's rating");
        }
    }

    @PutMapping("/update-win/{username}")
    public ResponseEntity<String> updateWin(@PathVariable String username) {
        try {
            validateUsername(username);
            logger.info("Passed validation, updating win");
            userService.updateUserWin(username);

            return ResponseEntity.ok("User win count updated successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while updating the user's win count");
        }
    }

    @PutMapping("/update-loss/{username}")
    public ResponseEntity<String> updateLoss(@PathVariable String username) {
        try {
            validateUsername(username);
            logger.info("Passed validation, updating loss");
            userService.updateUserLoss(username);

            return ResponseEntity.ok("User loss count updated successfully");
        } catch (ApiRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiRequestException("An error occurred while updating the user's loss count");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new ApiRequestException("Username cannot be null or empty");
        }
    }

    private void validateJwt(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            throw new InvalidTokenException("Invalid JWT");
        }
    }
}