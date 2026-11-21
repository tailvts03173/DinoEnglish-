package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByUsernameIgnoreCase(String username);
    Optional<User> findFirstByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
    long countByRole(Role role);
    long countByRoleAndStatus(Role role, UserStatus status);
    List<User> findByStatusOrderByTotalXpDesc(UserStatus status, Pageable pageable);
    List<User> findByStatusOrderByCurrentStreakDesc(UserStatus status, Pageable pageable);
}
