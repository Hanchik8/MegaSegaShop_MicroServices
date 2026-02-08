package org.example.megasegashop.auth.repository;

import jakarta.persistence.LockModeType;
import org.example.megasegashop.auth.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);
    boolean existsByEmail(String email);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthUser> findWithLockByEmail(String email);
}
