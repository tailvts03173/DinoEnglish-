package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Badge;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserBadge;
import com.example.dinoenglish.repository.BadgeRepository;
import com.example.dinoenglish.repository.UserBadgeRepository;
import com.example.dinoenglish.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dinoenglish.util.LevelUtil;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public GamificationService(UserRepository userRepository,
                               BadgeRepository badgeRepository,
                               UserBadgeRepository userBadgeRepository) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    /** Múi giờ Việt Nam, không phụ thuộc múi giờ của máy chủ. */
    public static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static LocalDate today() {
        return LocalDate.now(VN_ZONE);
    }

    /** Reset streak của tất cả người dùng đã bỏ học từ hôm kia trở về trước. */
    @Transactional
    public void resetBrokenStreaks() {
        userRepository.resetBrokenStreaks(today().minusDays(1));
    }

    @Transactional
    public void recordStudyActivity(User user, int xpEarned) {
        LocalDate today = today();
        LocalDate lastStudy = user.getLastStudyDate();

        if (lastStudy == null) {
            user.setCurrentStreak(1);
            user.setLastStudyDate(today);
        } else if (lastStudy.equals(today)) {
            // Already counted streak for today
        } else if (lastStudy.equals(today.minusDays(1))) {
            // Consecutive day!
            user.setCurrentStreak(user.getCurrentStreak() + 1);
            user.setLastStudyDate(today);
        } else {
            // Streak broken
            user.setCurrentStreak(1);
            user.setLastStudyDate(today);
        }

        if (user.getCurrentStreak() > user.getMaxStreak()) {
            user.setMaxStreak(user.getCurrentStreak());
        }

        // Add XP
        int newXp = (user.getTotalXp() != null ? user.getTotalXp() : 0) + xpEarned;
        user.setTotalXp(newXp);

        // Calculate Level (cùng bảng mốc với User/LevelUtil)
        user.setCurrentLevel(LevelUtil.levelForXp(newXp));

        userRepository.save(user);

        // Check Milestone Badges
        checkAndAwardBadges(user);
    }

    @Transactional
    public void checkAndAwardBadges(User user) {
        // First study badge
        awardBadgeIfEligible(user, "FIRST_STEP");

        // Streak badges
        if (user.getCurrentStreak() >= 7) {
            awardBadgeIfEligible(user, "STREAK_7");
        }
        if (user.getCurrentStreak() >= 14) {
            awardBadgeIfEligible(user, "STREAK_14");
        }
        if (user.getCurrentStreak() >= 30) {
            awardBadgeIfEligible(user, "STREAK_30");
        }

        // XP milestone
        if (user.getTotalXp() >= 500) {
            awardBadgeIfEligible(user, "XP_500");
        }
        if (user.getTotalXp() >= 1000) {
            awardBadgeIfEligible(user, "XP_1000");
        }
    }

    @Transactional
    public void awardBadgeIfEligible(User user, String badgeCode) {
        Optional<Badge> badgeOpt = badgeRepository.findByCode(badgeCode);
        if (badgeOpt.isPresent()) {
            Badge badge = badgeOpt.get();
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                UserBadge userBadge = UserBadge.builder()
                        .user(user)
                        .badge(badge)
                        .build();
                userBadgeRepository.save(userBadge);

                // Add bonus XP for earning badge
                if (badge.getXpBonus() != null && badge.getXpBonus() > 0) {
                    int newXp = (user.getTotalXp() != null ? user.getTotalXp() : 0) + badge.getXpBonus();
                    user.setTotalXp(newXp);
                    user.setCurrentLevel(LevelUtil.levelForXp(newXp));
                    userRepository.save(user);
                }
            }
        }
    }

    public List<User> getTopLearners(int limit) {
        return userRepository.findTopLearnersByXp(PageRequest.of(0, limit));
    }

    @Transactional
    public List<User> getTopStreaks(int limit) {
        resetBrokenStreaks();
        return userRepository.findTopStreaks(PageRequest.of(0, limit));
    }

    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByEarnedAtDesc(user);
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }
}
