package com.cs203.smucode.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

/**
 * @author: gav
 * @version: 1.0
 * @since: 2024-09-05
 */
@RestController
public class UserController {

    @GetMapping("/api/users/{id}")
    public ResponseEntity<String> getUser(@PathVariable Long id) {
        return ResponseEntity.ok("This will return a user: " + id);
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("This will login the user");
    }

    @PostMapping("/api/users/signup")
    public ResponseEntity<String> signup() {
        return ResponseEntity.ok("This will signup the user");
    }

    @PostMapping("/api/users/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("This will logout the user");
    }
}
