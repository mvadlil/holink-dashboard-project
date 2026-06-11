package com.example.holink.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.holink.entity.User;

public interface UserRepository extends JpaRepository<User, String> {
}
