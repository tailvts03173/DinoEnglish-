package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Badge;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    List<UserBadge> findByUserOrderByEarnedAtDesc(User user);

    List<UserBadge> findByUserIdOrderByEarnedAtDesc(Long userId);

    Optional<UserBadge> findByUserAndBadge(User user, Badge badge);

    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);
}
