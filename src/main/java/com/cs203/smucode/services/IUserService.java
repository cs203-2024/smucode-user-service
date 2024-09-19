package com.cs203.smucode.services;

import com.cs203.smucode.models.User;

import de.gesundkrank.jskills.Rating;

public interface IUserService {
    User getUserByUsername(String username);
    User createUser(User user);
    void deleteUser(Long id);
    void updateUserRating(String username, Rating newRating);
}