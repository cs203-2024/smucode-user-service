package com.cs203.smucode.services.impl;

import com.cs203.smucode.models.PlayerUser;
import com.cs203.smucode.models.AdminUser;
import com.cs203.smucode.models.User;
import com.cs203.smucode.services.ICRUDUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;

@Service
public class AuthUserServiceImpl implements UserDetailsService {
    private final ICRUDUserService userService;
    
    @Autowired
    public AuthUserServiceImpl(ICRUDUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (user.getAuthority().equals("PLAYER")) { 
            return new PlayerUser(user);
        } else if (user.getAuthority().equals("ADMIN")) {
            return new AdminUser(user);
        }

        return null;
    }
}