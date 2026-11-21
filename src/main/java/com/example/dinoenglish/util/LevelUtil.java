package com.example.dinoenglish.util;

/** Bảng mốc XP duy nhất cho toàn hệ thống: cấp 1 từ 0 XP, cấp 2 từ 150 XP, ... cấp 6 từ 2000 XP. */
public final class LevelUtil {

    private static final int[] THRESHOLDS = {0, 150, 350, 700, 1200, 2000};

    private LevelUtil() {
    }

    public static int levelForXp(Integer xp) {
        int value = xp != null ? xp : 0;
        int level = 1;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (value >= THRESHOLDS[i]) level = i + 1;
        }
        return level;
    }

    /** XP tuyệt đối của mốc cấp kế tiếp; ở cấp tối đa trả về mốc cấp tối đa. */
    public static int nextLevelThreshold(Integer xp) {
        int level = levelForXp(xp);
        return level >= THRESHOLDS.length ? THRESHOLDS[THRESHOLDS.length - 1] : THRESHOLDS[level];
    }

    public static int progressPercent(Integer xp) {
        int value = xp != null ? xp : 0;
        int level = levelForXp(value);
        if (level >= THRESHOLDS.length) return 100;
        int low = THRESHOLDS[level - 1];
        int high = THRESHOLDS[level];
        int percent = ((value - low) * 100) / (high - low);
        return Math.min(100, Math.max(0, percent));
    }
}
