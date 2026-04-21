package com.example.website.repository;

import com.example.website.models.WebUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<WebUser, Integer> {
    WebUser findByUsername(String username);
}