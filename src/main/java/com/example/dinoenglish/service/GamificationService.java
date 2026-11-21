package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Badge;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserBadge;
import com.example.dinoenglish.model.UserStatus;
import com.example.dinoenglish.repository.BadgeRepository;
import com.example.dinoenglish.repository.UserBadgeRepository;
import com.example.dinoenglish.repository.UserRepository;
import com.example.dinoenglish.util.LevelUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class GamificationService {

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final MongoTemplate mongoTemplate;

    public GamificationService(UserRepository userRepository,
                               BadgeRepository badgeRepository,
                               UserBadgeRepository userBadgeRepository,
                               MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /** Múi giờ Việt Nam, không phụ thuộc múi giờ của máy chủ. */
    public static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    public static LocalDate today() {
        return LocalDate.now(VN_ZONE);
    }

    /** Reset streak của tất cả người dùng đã bỏ học từ hôm kia trở về trước (một lệnh updateMany). */
    public void resetBrokenStreaks() {
        LocalDate cutoff = today().minusDays(1);
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("lastStudyDate").lt(cutoff).and("currentStreak").gt(0)),
                new Update().set("currentStreak", 0),
                User.class);
    }

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

    public void awardBadgeIfEligible(User user, String badgeCode) {
        Optional<Badge> badgeOpt = badgeRepository.findByCode(badgeCode);
        if (badgeOpt.isPresent()) {
            Badge badge = badgeOpt.get();
            if (!userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) {
                UserBadge userBadge = UserBadge.builder()
                        .userId(user.getId())
                        .badgeId(badge.getId())
                        .build();
                try {
                    userBadgeRepository.save(userBadge);
                } catch (DuplicateKeyException e) {
                    return; // đã được cấp bởi request song song (unique index userId + badgeId)
                }

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

    /** Bảng xếp hạng XP: các tài khoản đang hoạt động. (Muốn chỉ tính học viên, đổi sang lọc theo Role.USER.) */
    public List<User> getTopLearners(int limit) {
        return userRepository.findByStatusOrderByTotalXpDesc(UserStatus.ACTIVE, PageRequest.of(0, limit));
    }

    public List<User> getTopStreaks(int limit) {
        resetBrokenStreaks();
        return userRepository.findByStatusOrderByCurrentStreakDesc(UserStatus.ACTIVE, PageRequest.of(0, limit));
    }

    public List<UserBadge> getUserBadges(User user) {
        List<UserBadge> list = new ArrayList<>(userBadgeRepository.findByUserIdOrderByEarnedAtDesc(user.getId()));
        Set<String> badgeIds = new HashSet<>();
        for (UserBadge ub : list) badgeIds.add(ub.getBadgeId());
        Map<String, Badge> byId = new HashMap<>();
        for (Badge b : badgeRepository.findAllById(badgeIds)) byId.put(b.getId(), b);
        for (UserBadge ub : list) ub.setBadge(byId.get(ub.getBadgeId())); // nạp UserBadge.badge (@Transient)
        list.removeIf(ub -> ub.getBadge() == null);
        return list;
    }

    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }
}
