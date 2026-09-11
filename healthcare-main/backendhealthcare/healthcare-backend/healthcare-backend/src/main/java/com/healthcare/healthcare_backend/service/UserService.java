package com.healthcare.healthcare_backend.service;

import com.healthcare.healthcare_backend.dto.RegisterRequest;
import com.healthcare.healthcare_backend.entity.Role;
import com.healthcare.healthcare_backend.entity.User;
import com.healthcare.healthcare_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());

        try {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }

        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
}