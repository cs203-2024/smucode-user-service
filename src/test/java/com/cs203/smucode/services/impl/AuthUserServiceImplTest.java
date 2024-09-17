package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.User;
import com.cs203.smucode.models.PlayerUser;
import com.cs203.smucode.models.AdminUser;
import com.cs203.smucode.services.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUserServiceImplTest {

    @Mock
    private IUserService userService;

    private AuthUserServiceImpl authUserService;

    @BeforeEach
    void setUp() {
        System.out.println("Setting up test environment");
        MockitoAnnotations.openMocks(this);
        authUserService = new AuthUserServiceImpl(userService);
    }

    @Test
    @DisplayName("Should load player user by username")
    void loadPlayerUserByUsername() {
        String username = "player1";
        User user = new User(1L, username, "player@example.com", "password", null, "PLAYER");
        when(userService.getUserByUsername(username)).thenReturn(user);

        UserDetails userDetails = authUserService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof PlayerUser);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    @DisplayName("Should load admin user by username")
    void loadAdminUserByUsername() {
        String username = "admin1";
        User user = new User(2L, username, "admin@example.com", "password", null, "ADMIN");
        when(userService.getUserByUsername(username)).thenReturn(user);

        UserDetails userDetails = authUserService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof AdminUser);
        assertEquals(username, userDetails.getUsername());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void loadUserByUsernameNotFound() {
        String username = "nonexistent";
        when(userService.getUserByUsername(username)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> authUserService.loadUserByUsername(username));
    }

    @Test
    @DisplayName("Should throw IllegalStateException for unknown authority")
    void loadUserByUsernameUnknownAuthority() {
        String username = "unknown";
        User user = new User(3L, username, "unknown@example.com", "password", null, "UNKNOWN");
        when(userService.getUserByUsername(username)).thenReturn(user);

        assertThrows(IllegalStateException.class, () -> authUserService.loadUserByUsername(username));
    }
}