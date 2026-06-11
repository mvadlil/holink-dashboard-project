package com.example.holink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.holink.entity.User;
import com.example.holink.repository.UserRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            seedUser(userRepository, "user_001", "Kevin", "kevin@example.com");
            seedUser(userRepository, "user_002", "Sarah", "sarah@example.com");
        };
    }

    private void seedUser(UserRepository userRepository, String id, String name, String email) {
        if (userRepository.existsById(id)) {
            return;
        }

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        userRepository.save(user);
    }
}
