package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.User;
import com.cs203.smucode.models.UserRole;
import com.cs203.smucode.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private UserRepository userRepository;

    /** Mock object for PasswordEncoder. */
    @Mock
    private PasswordEncoder passwordEncoder;

    /** The UserServiceImpl instance to be tested. */
    private UserServiceImpl userService;
    
    /**
     * Sets up the test environment before each test method.
     * 
     * This method initializes mock objects and creates a new instance of UserServiceImpl.
     */
    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder);
    }

    /**
     * Tests the {@link UserServiceImpl#getUserByUsername(String)} method.
     * 
     * Verifies that the method correctly retrieves a user by their username.
     */
    @Test
    @DisplayName("Should retrieve user by username")
    void getUserByUsername() {
        String username = "testuser";
        User user = new User(1L, username,
                "player@example.com", "password", "gyat",
                UserRole.PLAYER,
                0.1, 0.2,0.3);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    /**
     * Tests the {@link UserServiceImpl#createUser(User)} method.
     * 
     * Verifies that the method correctly creates a new user with an encoded password.
     */
    @Test
    @DisplayName("Should create user")
    void createUser() {
        String username = "newuser";
        User user = new User(1L, username,
                "player@example.com", "password", "gyat",
                UserRole.PLAYER,
                0.1, 0.2,0.3);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(user);
    }

    /**
     * Tests the {@link UserServiceImpl#deleteUser(Long)} method.
     * 
     * Verifies that the method correctly calls the repository to delete a user by their ID.
     */
    @Test
    @DisplayName("Should delete user")
    void deleteUser() {
        Long userId = 1L;
        userService.deleteUser(userId);
        verify(userRepository).deleteById(userId);
    }
}