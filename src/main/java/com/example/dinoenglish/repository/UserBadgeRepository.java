package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends MongoRepository<UserBadge, String> {

    List<UserBadge> findByUserIdOrderByEarnedAtDesc(String userId);
    boolean existsByUserIdAndBadgeId(String userId, String badgeId);
    long countByUserId(String userId);
}
