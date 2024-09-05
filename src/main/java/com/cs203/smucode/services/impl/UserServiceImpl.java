package com.cs203.smucode.services.impl;

import com.cs203.smucode.services.IUserService;
import com.cs203.smucode.models.User;
import com.cs203.smucode.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: gav
 * @version: 1.0
 * @since: 2024-09-05
 */
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}