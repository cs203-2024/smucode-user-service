package com.cs203.smucode.services.impl;

import com.cs203.smucode.constants.TrueSkillConstants;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.models.UserProfile;
import com.cs203.smucode.repositories.UserProfileRepository;

import com.cs203.smucode.utils.AWSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.gesundkrank.jskills.Rating;
import software.amazon.awssdk.regions.Region;

import java.util.UUID;

/**
 * @author: gav
 * @version: 1.0
 * @since: 2024-09-05
 */
@Service
public class UserServiceImpl implements IUserService {
    private final UserProfileRepository userProfileRepository;

    @Autowired
    public UserServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfile getUserProfileByUsername(String username) {
        return userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("User not found with username: " + username));
    }

    @Override
    @Transactional
    public UserProfile createUserProfile(UserProfile userProfile) {

        // set default w/l records
        userProfile.setWins(TrueSkillConstants.DEFAULT_WINS);
        userProfile.setLosses(TrueSkillConstants.DEFAULT_LOSES);

        //set the default TrueSkill parameters
        userProfile.setMu(TrueSkillConstants.DEFAULT_MU);
        userProfile.setSigma(TrueSkillConstants.DEFAULT_SIGMA);
        userProfile.setSkillIndex(calculateSkillIndex(TrueSkillConstants.DEFAULT_MU, TrueSkillConstants.DEFAULT_SIGMA));

        return userProfileRepository.save(userProfile);
    }

    @Override
    @Transactional
    public void deleteUserProfile(UUID id) {
        userProfileRepository.deleteById(id);
    }

    // Profile picture related
    @Override
    @Transactional
    public void uploadProfilePicture(String username, String imageUrl) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Profile not found"));

        userProfile.setProfileImageUrl(imageUrl);
        userProfileRepository.save(userProfile);
    }

    //trueSkill-related methods
    @Override
    @Transactional
    public void updateUserRating(String username, Rating newRating) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("User not found with username: " + username));
        userProfile.setMu(newRating.getMean());
        userProfile.setSigma(newRating.getStandardDeviation());
        userProfile.setSkillIndex(calculateSkillIndex(userProfile.getMu(), userProfile.getSigma()));
        userProfileRepository.save(userProfile);
    }

    @Override
    @Transactional
    public void updateUserWin(String username) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("User not found with username: " + username));

        userProfile.setWins(userProfile.getWins() + 1);
        userProfileRepository.save(userProfile);
    }

    @Override
    @Transactional
    public void updateUserLoss(String username) {
        UserProfile userProfile = userProfileRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("User not found with username: " + username));

        userProfile.setLosses(userProfile.getLosses() + 1);
        userProfileRepository.save(userProfile);
    }

    private double calculateSkillIndex(double mu, double sigma) {
        return mu - TrueSkillConstants.K_FACTOR * sigma;
    }
}