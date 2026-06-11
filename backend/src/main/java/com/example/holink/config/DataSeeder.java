package com.example.holink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.holink.entity.User;
import com.example.holink.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(userRepository, passwordEncoder, "user_001", "Kevin", "kevin@example.com", "password123");
            seedUser(userRepository, passwordEncoder, "user_002", "Sarah", "sarah@example.com", "password123");
        };
    }

    private void seedUser(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          String id,
                          String name,
                          String email,
                          String rawPassword) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser != null) {
            boolean updated = false;

            if (!name.equals(existingUser.getName())) {
                existingUser.setName(name);
                updated = true;
            }
            if (!email.equals(existingUser.getEmail())) {
                existingUser.setEmail(email);
                updated = true;
            }
            if (existingUser.getPasswordHash() == null
                    || !passwordEncoder.matches(rawPassword, existingUser.getPasswordHash())) {
                existingUser.setPasswordHash(passwordEncoder.encode(rawPassword));
                updated = true;
            }

            if (updated) {
                userRepository.save(existingUser);
            }
            return;
        }

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
