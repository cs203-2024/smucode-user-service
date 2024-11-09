package com.cs203.smucode.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.exception.InvalidTokenException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.cs203.smucode.constants.MediaConstants;
import com.cs203.smucode.constants.OAuth2Constants;
import com.cs203.smucode.dto.*;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.utils.AWSUtil;
import com.cs203.smucode.mappers.UserProfileMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gesundkrank.jskills.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserProfileMapper.class})
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IUserService userService;

    @MockBean
    private UserProfileMapper userProfileMapper;

    @MockBean
    private AWSUtil awsUtil;

    private UserProfile testProfile;
    private UserInfoDTO testUserInfoDTO;
    private UserIdentificationDTO testUserIdDTO;
    private UserRatingDTO testUserRatingDTO;
    private final UUID testId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testProfile = new UserProfile();
        testProfile.setId(testId);
        testProfile.setUsername("testUser");
        testProfile.setEmail("test@example.com");
        testProfile.setProfileImageUrl("https://example.com/image.jpg");
        testProfile.setWins(5);
        testProfile.setLosses(3);
        testProfile.setMu(25.0);
        testProfile.setSigma(8.333);
        testProfile.setSkillIndex(0.0);

        testUserInfoDTO = new UserInfoDTO(
                testId,
                "testUser",
                "test@example.com",
                "https://example.com/image.jpg",
                5,
                3,
                25.0,
                8.333,
                0.0
        );

        testUserIdDTO = new UserIdentificationDTO(
                testId,
                "testUser",
                "test@example.com"
        );

        testUserRatingDTO = new UserRatingDTO(
                "testUser",
                25.0,
                8.333,
                0.0
        );

        when(userProfileMapper.userProfileToUserInfoDTO(any(UserProfile.class)))
                .thenReturn(testUserInfoDTO);
        when(userProfileMapper.userIdentificationDTOtoUserProfile(any(UserIdentificationDTO.class)))
                .thenReturn(testProfile);
    }

    @Test
    @WithMockUser
    @DisplayName("Should get user profile successfully")
    void getProfile_ValidUsername_Success() throws Exception {
        when(userService.getUserProfileByUsername("testUser"))
                .thenReturn(testProfile);

        mockMvc.perform(get("/api/users/profile/testUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("$.wins").value(5))
                .andExpect(jsonPath("$.losses").value(3));
    }

    @Test
    @WithMockUser
    @DisplayName("Should create user profile successfully")
    void createUser_ValidData_Success() throws Exception {
        when(userService.createUserProfile(any(UserProfile.class)))
                .thenReturn(testProfile);

        mockMvc.perform(post("/api/users/profile/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserIdDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(content().string(containsString("Created user profile")));
    }

    @Test
    @WithMockUser
    @DisplayName("Should delete user profile successfully")
    void deleteUser_ValidData_Success() throws Exception {
        when(userService.getUserProfileByUsername(anyString()))
                .thenReturn(testProfile);

        mockMvc.perform(post("/api/users/profile/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserIdDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Deleted user profile")));
    }

    @Test
    @WithMockUser
    @DisplayName("Should get pre-signed URL successfully")
    void getPreSignedUrl_ValidData_Success() throws Exception {
        String preSignedUrl = "https://presigned-url.com";
        String key = "profile-picture/testUser-123";

        when(awsUtil.generatePresignedUrl(anyString(), anyString())).thenReturn(preSignedUrl);
        when(awsUtil.getKey(anyString())).thenReturn(key);

        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", MediaConstants.IMAGE_JPEG))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preSignedUrl").value(preSignedUrl))
                .andExpect(jsonPath("$.key").value(key));
    }

    @Test
    @WithMockUser
    @DisplayName("Should upload picture successfully")
    void uploadPicture_ValidData_Success() throws Exception {
        String key = "profile-picture/testUser-123";
        String imageUrl = "https://example.com/image.jpg";

        when(awsUtil.getKey(anyString())).thenReturn(key);
        when(awsUtil.getObjectUrl(anyString())).thenReturn(imageUrl);

        mockMvc.perform(post("/api/users/upload-picture")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("key", key))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.imageUrl").value(imageUrl));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update rating successfully")
    void updateRating_ValidData_Success() throws Exception {
        mockMvc.perform(put("/api/users/update-rating")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRatingDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("User rating updated successfully"));

        verify(userService).updateUserRating(eq("testUser"), any(Rating.class));
    }

    @Test
    @WithMockUser
    @DisplayName("Should update win count successfully")
    void updateWin_ValidUsername_Success() throws Exception {
        mockMvc.perform(put("/api/users/update-win/{username}", "testUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User win count updated successfully"));

        verify(userService).updateUserWin("testUser");
    }

    @Test
    @WithMockUser
    @DisplayName("Should update loss count successfully")
    void updateLoss_ValidUsername_Success() throws Exception {
        mockMvc.perform(put("/api/users/update-loss/{username}", "testUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User loss count updated successfully"));

        verify(userService).updateUserLoss("testUser");
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when content type is invalid")
    void getPreSignedUrl_InvalidContentType_BadRequest() throws Exception {
        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", "invalid/type"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should return 400 when key format is invalid")
    void uploadPicture_InvalidKeyFormat_BadRequest() throws Exception {
        mockMvc.perform(post("/api/users/upload-picture")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("key", "invalid-key"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when getting profile fails")
    void getProfile_WhenServiceThrowsException_ThrowsApiRequestException() throws Exception {
        when(userService.getUserProfileByUsername(anyString()))
                .thenThrow(new ApiRequestException("An error occurred while fetching the user profile"));

        mockMvc.perform(get("/api/users/profile/{username}", "testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while fetching the user profile",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when creating profile fails")
    void createUser_WhenServiceThrowsException_ThrowsApiRequestException() throws Exception {
        when(userService.createUserProfile(any(UserProfile.class)))
                .thenThrow(new ApiRequestException("An error occurred while creating the user profile"));

        mockMvc.perform(post("/api/users/profile/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserIdDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while creating the user profile",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when deleting profile fails with UsernameNotFoundException")
    void deleteUser_WhenUserNotFound_ThrowsApiRequestException() throws Exception {
        when(userService.getUserProfileByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("Username not found"));

        mockMvc.perform(post("/api/users/profile/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserIdDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("Username not found",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when getting pre-signed URL with invalid content type")
    void getPreSignedUrl_InvalidContentType_ThrowsApiRequestException() throws Exception {
        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", "invalid/type"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("Unsupported content type: invalid/type",
                        result.getResolvedException().getMessage()));
    }

//    @Test
//    @WithMockUser
//    @DisplayName("Should throw ApiRequestException when missing content type")
//    void getPreSignedUrl_MissingContentType_ThrowsApiRequestException() throws Exception {
//        mockMvc.perform(post("/api/users/get-upload-link")
//                        .with(csrf())
//                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser"))))
//                .andExpect(status().isBadRequest())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
//                .andExpect(result -> assertEquals("Content type is mandatory",
//                        result.getResolvedException().getMessage()));
//    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when AWS operations fail")
    void getPreSignedUrl_WhenAwsOperationFails_ThrowsApiRequestException() throws Exception {
        when(awsUtil.generatePresignedUrl(anyString(), anyString()))
                .thenThrow(new RuntimeException("AWS operation failed"));

        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", MediaConstants.IMAGE_JPEG))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while uploading the profile picture",
                        result.getResolvedException().getMessage()));
    }

//    @Test
//    @WithMockUser
//    @DisplayName("Should throw InvalidTokenException when JWT is null")
//    void uploadPicture_NullJwt_ThrowsInvalidTokenException() throws Exception {
//        mockMvc.perform(post("/api/users/upload-picture")
//                        .with(csrf())
//                        .param("key", "profile-picture/test-123"))
//                .andExpect(status().isUnauthorized())
//                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidTokenException))
//                .andExpect(result -> assertEquals("Invalid JWT",
//                        result.getResolvedException().getMessage()));
//    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when upload picture with empty key")
    void uploadPicture_EmptyKey_ThrowsApiRequestException() throws Exception {
        mockMvc.perform(post("/api/users/upload-picture")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("key", ""))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("Please provide a non-empty or null key",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when key format is invalid")
    void uploadPicture_InvalidKeyFormat_ThrowsApiRequestException() throws Exception {
        mockMvc.perform(post("/api/users/upload-picture")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("key", "invalid-key"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("Invalid key: invalid-key, please provide a valid key",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when updating rating fails")
    void updateRating_WhenServiceThrowsException_ThrowsApiRequestException() throws Exception {
        doThrow(new ApiRequestException("An error occurred while updating the rating"))
                .when(userService).updateUserRating(anyString(), any(Rating.class));

        mockMvc.perform(put("/api/users/update-rating")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRatingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while updating the rating",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when updating win count fails")
    void updateWin_WhenServiceThrowsException_ThrowsApiRequestException() throws Exception {
        doThrow(new ApiRequestException("An error occurred while updating win count"))
                .when(userService).updateUserWin(anyString());

        mockMvc.perform(put("/api/users/update-win/{username}", "testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while updating win count",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should throw ApiRequestException when updating loss count fails")
    void updateLoss_WhenServiceThrowsException_ThrowsApiRequestException() throws Exception {
        doThrow(new ApiRequestException("An error occurred while updating loss count"))
                .when(userService).updateUserLoss(anyString());

        mockMvc.perform(put("/api/users/update-loss/{username}", "testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ApiRequestException))
                .andExpect(result -> assertEquals("An error occurred while updating loss count",
                        result.getResolvedException().getMessage()));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in getProfile")
    void getProfile_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        when(userService.getUserProfileByUsername(anyString()))
                .thenThrow(new RuntimeException("Some internal error"));

        mockMvc.perform(get("/api/users/profile/testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while fetching the user profile"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in createUser")
    void createUser_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        when(userService.createUserProfile(any(UserProfile.class)))
                .thenThrow(new RuntimeException("Some internal error"));

        mockMvc.perform(post("/api/users/profile/create")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserIdDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while fetching the user profile"));
    }

    @Test
    @DisplayName("Should handle null content type in getPreSignedUrl")
    void getPreSignedUrl_NullContentType_ThrowsApiRequestException() throws Exception {
        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser"))))  // No contentType parameter
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle empty content type in getPreSignedUrl")
    void getPreSignedUrl_EmptyContentType_ThrowsApiRequestException() throws Exception {
        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Content type is mandatory"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in getPreSignedUrl")
    void getPreSignedUrl_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        when(awsUtil.generatePresignedUrl(anyString(), anyString()))
                .thenThrow(new RuntimeException("AWS error"));

        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf())
                        .with(jwt().jwt(t -> t.claim(OAuth2Constants.SUBJECT, "testUser")))
                        .param("contentType", MediaConstants.IMAGE_JPEG))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while uploading the profile picture"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in updateRating")
    void updateRating_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        doThrow(new RuntimeException("Rating update error"))
                .when(userService).updateUserRating(anyString(), any(Rating.class));

        mockMvc.perform(put("/api/users/update-rating")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUserRatingDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while updating the user's rating"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in updateWin")
    void updateWin_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        doThrow(new RuntimeException("Win update error"))
                .when(userService).updateUserWin(anyString());

        mockMvc.perform(put("/api/users/update-win/testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while updating the user's win count"));
    }

    @Test
    @WithMockUser
    @DisplayName("Should handle general exception in updateLoss")
    void updateLoss_WhenGeneralException_ThrowsApiRequestException() throws Exception {
        doThrow(new RuntimeException("Loss update error"))
                .when(userService).updateUserLoss(anyString());

        mockMvc.perform(put("/api/users/update-loss/testUser")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("An error occurred while updating the user's loss count"));
    }

    @Test
    @DisplayName("Should handle null JWT validation")
    void validateJwt_NullJwt_ThrowsInvalidTokenException() throws Exception {
        mockMvc.perform(post("/api/users/get-upload-link")
                        .with(csrf()))  // No JWT provided
                .andExpect(status().isUnauthorized());
    }

}