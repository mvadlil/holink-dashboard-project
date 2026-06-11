package com.example.holink.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.holink.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {

    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByUserId(String userId);

    boolean existsByUsername(String username);
}
