package com.example.dinoenglish.repository;

import com.example.dinoenglish.model.Role;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    List<User> findByRole(Role role);

    List<User> findByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.status = 'ACTIVE' ORDER BY u.totalXp DESC")
    List<User> findTopLearnersByXp(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'USER' AND u.status = 'ACTIVE' ORDER BY u.currentStreak DESC")
    List<User> findTopStreaks(Pageable pageable);

    long countByRole(Role role);

    long countByRoleAndStatus(Role role, UserStatus status);

    /** Đặt lại streak về 0 cho người đã bỏ học (lần học cuối trước hôm qua). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.currentStreak = 0 WHERE u.currentStreak > 0 AND (u.lastStudyDate IS NULL OR u.lastStudyDate < :yesterday)")
    int resetBrokenStreaks(@Param("yesterday") LocalDate yesterday);
}
