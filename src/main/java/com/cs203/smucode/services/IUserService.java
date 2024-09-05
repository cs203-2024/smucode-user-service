package com.cs203.smucode.services;

import com.cs203.smucode.models.User;

public interface IUserService {
    User getUserById(Long id);
    User createUser(User user);
    void deleteUser(Long id);
}