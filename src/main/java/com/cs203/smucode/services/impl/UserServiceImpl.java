package com.cs203.smucode.services.impl;

import com.cs203.smucode.constants.TrueSkillConstants;
import com.cs203.smucode.exception.ApiRequestException;
import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.models.User;
import com.cs203.smucode.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import de.gesundkrank.jskills.Rating;

import java.util.Optional;

/**
 * @author: gav
 * @version: 1.0
 * @since: 2024-09-05
 */
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //set the default TrueSkill parameters
        user.setMu(TrueSkillConstants.DEFAULT_MU);
        user.setSigma(TrueSkillConstants.DEFAULT_SIGMA);
        user.setSkillIndex(calculateSkillIndex(TrueSkillConstants.DEFAULT_MU, TrueSkillConstants.DEFAULT_SIGMA));

        return userRepository.save(user);
    }

    @Override   
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    //trueSkill-related methods
    @Override
    @Transactional
    public void updateUserRating(String username, Rating newRating) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiRequestException("User not found with username: " + username));
        user.setMu(newRating.getMean());
        user.setSigma(newRating.getStandardDeviation());
        user.setSkillIndex(calculateSkillIndex(user.getMu(), user.getSigma()));
        userRepository.save(user);
    }

    private double calculateSkillIndex(double mu, double sigma) {
        return mu - TrueSkillConstants.K_FACTOR * sigma;
    }
}