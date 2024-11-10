package com.cs203.smucode.controllers;

import com.cs203.smucode.configs.TestSecurityConfiguration;
import com.cs203.smucode.constants.MediaConstants;
import com.cs203.smucode.dto.*;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.repositories.UserProfileRepository;
import com.cs203.smucode.utils.AWSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfiguration.class)
@DisplayName("UserController Integration Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AWSUtil awsUtil;

    @Value("${feign.access.token}")
    private String TEST_JWT;
    private UserProfile testUser;
    private UserIdentificationDTO testUserIdDTO;
    private UserRatingDTO testUserRatingDTO;
    private final UUID testId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAll();

        testUser = new UserProfile();
        testUser.setId(testId);
        testUser.setUsername("SYSTEM");
        testUser.setEmail("test@example.com");
        testUser.setProfileImageUrl("https://example.com/image.jpg");
        testUser.setWins(5);
        testUser.setLosses(3);
        testUser.setMu(25.0);
        testUser.setSigma(8.333);
        testUser.setSkillIndex(0.0);

        testUserIdDTO = new UserIdentificationDTO(
                testId,
                "SYSTEM",
                "test@example.com"
        );

        testUserRatingDTO = new UserRatingDTO(
                "SYSTEM",
                25.0,
                8.333,
                0.0
        );
    }

    @AfterEach
    void tearDown() {
        userProfileRepository.deleteAll();
    }

    @Nested
    @DisplayName("Profile Operations")
    class ProfileOperations {
        @Test
        @DisplayName("Should get user profile successfully")
        void getProfile_ValidUsername_Success() throws Exception {
            userProfileRepository.save(testUser);

            mockMvc.perform(get("/api/users/profile/SYSTEM")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testId.toString()))
                    .andExpect(jsonPath("$.username").value("SYSTEM"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(jsonPath("$.profileImageUrl").value("https://example.com/image.jpg"))
                    .andExpect(jsonPath("$.wins").value(5))
                    .andExpect(jsonPath("$.losses").value(3));
        }

        @Test
        @DisplayName("Should return 400 when username not found")
        void getProfile_InvalidUsername_ReturnsBadRequest() throws Exception {
            mockMvc.perform(get("/api/users/profile/nonexistent")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertThat(result.getResolvedException())
                            .isInstanceOf(ApiRequestException.class));
        }

        @Test
        @DisplayName("Should create user profile successfully")
        void createUser_ValidData_Success() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/users/profile/create")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserIdDTO)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(content().string(containsString("Created user profile")))
                    .andReturn();

            // Verify the user was actually created in the database
            UserProfile savedUser = userProfileRepository.findByUsername("SYSTEM").orElseThrow();
            assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(savedUser.getMu()).isEqualTo(25.0);
            assertThat(savedUser.getSigma()).isEqualTo(8.333);

            // Verify Location header format
            String location = result.getResponse().getHeader("Location");
            assertThat(location).contains("/api/users/profile/SYSTEM");
        }
    }

    @Nested
    @DisplayName("Image Upload Operations")
    class ImageUploadOperations {
        @Test
        @DisplayName("Should get pre-signed URL successfully")
        void getPreSignedUrl_ValidRequest_Success() throws Exception {
            mockMvc.perform(post("/api/users/get-upload-link")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .param("contentType", MediaConstants.IMAGE_JPEG))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.key").exists())
                    .andExpect(jsonPath("$.preSignedUrl").exists());
        }

        @Test
        @DisplayName("Should reject invalid content type")
        void getPreSignedUrl_InvalidContentType_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/users/get-upload-link")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .param("contentType", "invalid/type"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should reject invalid key format")
        void uploadPicture_InvalidKey_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/users/upload-picture")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .param("key", "invalid-key"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("User Stats Operations")
    class UserStatsOperations {
        @Test
        @DisplayName("Should update rating successfully")
        void updateRating_ValidData_Success() throws Exception {
            userProfileRepository.save(testUser);

            mockMvc.perform(put("/api/users/update-rating")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testUserRatingDTO)))
                    .andDo(print())
                    .andExpect(status().isOk());

            UserProfile updatedUser = userProfileRepository.findByUsername("SYSTEM").orElseThrow();
            assertThat(updatedUser.getMu()).isEqualTo(25.0);
            assertThat(updatedUser.getSigma()).isEqualTo(8.333);
        }

        @Test
        @DisplayName("Should update win count successfully")
        void updateWin_ValidUsername_Success() throws Exception {
            userProfileRepository.save(testUser);

            mockMvc.perform(put("/api/users/update-win/SYSTEM")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk());

            UserProfile updatedUser = userProfileRepository.findByUsername("SYSTEM").orElseThrow();
            assertThat(updatedUser.getWins()).isEqualTo(6); // 5 + 1
        }

        @Test
        @DisplayName("Should update loss count successfully")
        void updateLoss_ValidUsername_Success() throws Exception {
            userProfileRepository.save(testUser);

            mockMvc.perform(put("/api/users/update-loss/SYSTEM")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isOk());

            UserProfile updatedUser = userProfileRepository.findByUsername("SYSTEM").orElseThrow();
            assertThat(updatedUser.getLosses()).isEqualTo(4); // 3 + 1
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {
        @Test
        @DisplayName("Should handle unauthorized access")
        void anyEndpoint_NoToken_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/users/profile/SYSTEM"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void getPreSignedUrl_MissingContentType_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/users/get-upload-link")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty content type")
        void getPreSignedUrl_EmptyContentType_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/users/get-upload-link")
                            .header("Authorization", "Bearer " + TEST_JWT)
                            .param("contentType", ""))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertThat(result.getResolvedException().getMessage())
                            .contains("Content type is mandatory"));
        }

        @Test
        @DisplayName("Should handle missing key in upload")
        void uploadPicture_MissingKey_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/users/upload-picture")
                            .header("Authorization", "Bearer " + TEST_JWT))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}