package com.example.holink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.holink.entity.Link;

public interface LinkRepository extends JpaRepository<Link, String> {

    List<Link> findByProfileIdOrderByPositionAsc(String profileId);

    List<Link> findByProfileIdAndIsActiveTrueOrderByPositionAsc(String profileId);
}
