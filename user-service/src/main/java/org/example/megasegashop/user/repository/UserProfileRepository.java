package org.example.megasegashop.user.repository;

import org.example.megasegashop.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByEmail(String email);
    Optional<UserProfile> findByAuthUserId(Long authUserId);
    boolean existsByEmail(String email);
}
