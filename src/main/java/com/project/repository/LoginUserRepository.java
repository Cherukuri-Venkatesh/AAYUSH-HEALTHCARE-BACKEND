package com.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.entity.LoginUser;
import com.project.entity.LoginUserId;

public interface LoginUserRepository extends JpaRepository<LoginUser, LoginUserId> {
    Optional<LoginUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    void deleteByEmailIgnoreCase(String email);
}
