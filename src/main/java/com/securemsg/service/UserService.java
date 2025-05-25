package com.securemsg.service;

import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // Ensure IIN is unique (IIN used as ID)
        if (userRepository.findById(user.getIin()).isPresent()) {
            throw new IllegalArgumentException("A user with this IIN already exists.");
        }
        // Ensure email is unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use by another account.");
        }
        // Normalize and validate role
        String role = (user.getRole() != null ? user.getRole().toUpperCase() : "STUDENT");
        if (!role.equals("STUDENT") && !role.equals("TEACHER")) {
            throw new IllegalArgumentException("Invalid role selection.");
        }
        user.setRole(role);
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Set default preferences if not provided
        if (user.getTheme() == null) user.setTheme("light");
        if (user.getLanguage() == null) user.setLanguage("en");
        // Save to MongoDB
        return userRepository.save(user);
    }

    public Optional<User> getByIIN(String iin) {
        return userRepository.findById(iin);
    }

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
