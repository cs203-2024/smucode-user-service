package com.cs203.smucode.services.impl;

import com.cs203.smucode.constants.TrueSkillConstants;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.repositories.UserProfileRepository;
import de.gesundkrank.jskills.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Test Suite")
class UserServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserProfile testUserProfile;
    private final String testUsername = "testUser";
    private final UUID testId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUserProfile = new UserProfile();
        testUserProfile.setId(testId);
        testUserProfile.setUsername(testUsername);
        testUserProfile.setEmail("test@example.com");
        testUserProfile.setMu(TrueSkillConstants.DEFAULT_MU);
        testUserProfile.setSigma(TrueSkillConstants.DEFAULT_SIGMA);
        testUserProfile.setWins(0);
        testUserProfile.setLosses(0);
    }

    @Test
    @DisplayName("Should retrieve user profile when username exists")
    void getUserProfileByUsername_ValidUsername_ReturnsUserProfile() {
        // Arrange
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUserProfile));

        // Act
        UserProfile result = userService.getUserProfileByUsername(testUsername);

        // Assert
        assertNotNull(result);
        assertEquals(testUsername, result.getUsername());
        assertEquals(testId, result.getId());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("Should throw exception when username doesn't exist")
    void getUserProfileByUsername_InvalidUsername_ThrowsException() {
        // Arrange
        String invalidUsername = "nonexistent";
        when(userProfileRepository.findByUsername(invalidUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.getUserProfileByUsername(invalidUsername));
        assertEquals("User not found with username: " + invalidUsername, exception.getMessage());

        // Verify
        verify(userProfileRepository).findByUsername(invalidUsername);
    }

    @Test
    @DisplayName("Should create user profile with default values")
    void createUserProfile_ValidProfile_Success() {
        // Arrange
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenReturn(testUserProfile);

        // Act
        UserProfile result = userService.createUserProfile(testUserProfile);

        // Assert
        assertNotNull(result);
        assertEquals(TrueSkillConstants.DEFAULT_MU, result.getMu());
        assertEquals(TrueSkillConstants.DEFAULT_SIGMA, result.getSigma());
        assertEquals(TrueSkillConstants.DEFAULT_WINS, result.getWins());
        assertEquals(TrueSkillConstants.DEFAULT_LOSES, result.getLosses());
        assertEquals(TrueSkillConstants.DEFAULT_MU - TrueSkillConstants.K_FACTOR * TrueSkillConstants.DEFAULT_SIGMA,
                result.getSkillIndex());

        // Verify
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("Should delete user profile when ID exists")
    void deleteUserProfile_ValidId_Success() {
        // Act
        userService.deleteUserProfile(testId);

        // Verify
        verify(userProfileRepository).deleteById(testId);
    }

    @Test
    @DisplayName("Should upload profile picture when username exists")
    void uploadProfilePicture_ValidUsername_Success() {
        // Arrange
        String imageUrl = "https://example.com/image.jpg";
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenReturn(testUserProfile);

        // Act
        userService.uploadProfilePicture(testUsername, imageUrl);

        // Assert
        assertEquals(imageUrl, testUserProfile.getProfileImageUrl());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("Should throw exception when uploading picture for non-existent user")
    void uploadProfilePicture_InvalidUsername_ThrowsException() {
        // Arrange
        String imageUrl = "https://example.com/image.jpg";
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.uploadProfilePicture(testUsername, imageUrl));

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update user rating when username exists")
    void updateUserRating_ValidUsername_Success() {
        // Arrange
        double newMu = 30.0;
        double newSigma = 5.0;
        Rating newRating = new Rating(newMu, newSigma);

        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenReturn(testUserProfile);

        // Act
        userService.updateUserRating(testUsername, newRating);

        // Assert
        assertEquals(newMu, testUserProfile.getMu());
        assertEquals(newSigma, testUserProfile.getSigma());
        assertEquals(newMu - TrueSkillConstants.K_FACTOR * newSigma, testUserProfile.getSkillIndex());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("Should throw exception when updating rating for non-existent user")
    void updateUserRating_InvalidUsername_ThrowsException() {
        // Arrange
        Rating newRating = new Rating(30.0, 5.0);
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserRating(testUsername, newRating));
        assertEquals("User not found with username: " + testUsername, exception.getMessage());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increment win count when username exists")
    void updateUserWin_ValidUsername_Success() {
        // Arrange
        testUserProfile.setWins(5);
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenReturn(testUserProfile);

        // Act
        userService.updateUserWin(testUsername);

        // Assert
        assertEquals(6, testUserProfile.getWins());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("Should throw exception when updating win count for non-existent user")
    void updateUserWin_InvalidUsername_ThrowsException() {
        // Arrange
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserWin(testUsername));
        assertEquals("User not found with username: " + testUsername, exception.getMessage());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increment loss count when username exists")
    void updateUserLoss_ValidUsername_Success() {
        // Arrange
        testUserProfile.setLosses(3);
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.of(testUserProfile));
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenReturn(testUserProfile);

        // Act
        userService.updateUserLoss(testUsername);

        // Assert
        assertEquals(4, testUserProfile.getLosses());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository).save(testUserProfile);
    }

    @Test
    @DisplayName("Should throw exception when updating loss count for non-existent user")
    void updateUserLoss_InvalidUsername_ThrowsException() {
        // Arrange
        when(userProfileRepository.findByUsername(testUsername))
                .thenReturn(Optional.empty());

        // Act & Assert
        ApiRequestException exception = assertThrows(ApiRequestException.class,
                () -> userService.updateUserLoss(testUsername));
        assertEquals("User not found with username: " + testUsername, exception.getMessage());

        // Verify
        verify(userProfileRepository).findByUsername(testUsername);
        verify(userProfileRepository, never()).save(any());
    }
}