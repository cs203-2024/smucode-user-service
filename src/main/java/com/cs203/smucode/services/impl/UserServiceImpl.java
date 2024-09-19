package com.cs203.smucode.services.impl;

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

    //default TrueSkill parameters
    private static final double DEFAULT_MU = 25.0;
    private static final double DEFAULT_SIGMA = 8.333; //default s.d.
    private static final int K_FACTOR = 3; //"confidence" parameter

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
        user.setMu(DEFAULT_MU);
        user.setSigma(DEFAULT_SIGMA);
        user.setSkillIndex(calculateSkillIndex(DEFAULT_MU, DEFAULT_SIGMA));

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
    public void updateUserRating(Long userId, Rating newRating) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        User user = optionalUser.get();
        user.setMu(newRating.getMean());
        user.setSigma(newRating.getStandardDeviation());
        user.setSkillIndex(calculateSkillIndex(user.getMu(), user.getSigma()));
        userRepository.save(user);
    }

    private double calculateSkillIndex(double mu, double sigma) {
        return mu - K_FACTOR * sigma;
    }
}