package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(String userId);
    List<PasswordResetToken> findByUserIdAndUsedFalse(String userId);
    long countByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);   // giới hạn số lần yêu cầu
    void deleteByUserId(String userId);
}
