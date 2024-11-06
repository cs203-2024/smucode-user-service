package com.cs203.smucode.services;

import com.cs203.smucode.models.UserProfile;

import de.gesundkrank.jskills.Rating;

import java.util.UUID;

public interface IUserService {
    UserProfile getUserProfileByUsername(String username);
    UserProfile createUserProfile(UserProfile userProfile);
    void deleteUserProfile(UUID id);
    void updateUserRating(String username, Rating newRating);
    void uploadProfilePicture(String username, String imageUrl);
    void updateUserWin(String username);
    void updateUserLoss(String username);
}