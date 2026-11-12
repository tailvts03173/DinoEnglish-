package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.PasswordResetToken;
import com.example.dinoenglish.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
