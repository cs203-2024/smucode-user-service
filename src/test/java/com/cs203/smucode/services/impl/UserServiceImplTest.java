package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.repositories.UserProfileRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author gav
 * @version 1.0
 * @since 2024-09-07
 * Unit tests for the {@link UserServiceImpl} class.
 * 
 * This class contains test methods to verify the functionality of the UserServiceImpl.
 */

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    /** Mock object for UserRepository. */
    @Mock
    private UserProfileRepository userProfileRepository;

    /** The UserServiceImpl instance to be tested. */
    private UserServiceImpl userService;
    
    /**
     * Sets up the test environment before each test method.
     * 
     * This method initializes mock objects and creates a new instance of UserServiceImpl.
     */
    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userProfileRepository);
    }

    /**
     * Tests the {@link UserServiceImpl#getUserProfileByUsername(String)} method.
     * 
     * Verifies that the method correctly retrieves a user by their username.
     */
    @Test
    @DisplayName("Should retrieve user by username")
    void getUserByUsername() {
        String username = "testuser";
        UserProfile user = new UserProfile(UUID.randomUUID(), username,
                "player@example.com", null,
                0, 0,
                0.1, 0.2,0.3);
        when(userProfileRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserProfile result = userService.getUserProfileByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    /**
     * Tests the {@link UserServiceImpl#createUserProfile(UserProfile)} method.
     * 
     * Verifies that the method correctly creates a new user with an encoded password.
     */
    @Test
    @DisplayName("Should create user")
    void createUser() {
        String username = "newuser";
        UserProfile userProfile = new UserProfile(UUID.fromString("ff6218d9-8bc1-460f-b3f3-9b2ac4f4561b"), username,
                "player@example.com", null,
                0,0,
                0.1, 0.2,0.3);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile result = userService.createUserProfile(userProfile);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(userProfileRepository).save(userProfile);
    }

    /**
     * Tests the {@link UserServiceImpl#deleteUserProfile(UUID)} method.
     * 
     * Verifies that the method correctly calls the repository to delete a user by their ID.
     */
    @Test
    @DisplayName("Should delete user")
    void deleteUser() {
        UUID userId = UUID.fromString("ff6218d9-8bc1-460f-b3f3-9b2ac4f4561b");
        userService.deleteUserProfile(userId);
        verify(userProfileRepository).deleteById(userId);
    }
}