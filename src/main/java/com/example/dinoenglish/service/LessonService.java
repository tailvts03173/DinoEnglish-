package com.example.dinoenglish.service;

import com.example.dinoenglish.model.*;
import com.example.dinoenglish.repository.CourseRepository;
import com.example.dinoenglish.repository.LessonRepository;
import com.example.dinoenglish.repository.QuizSubmissionRepository;
import com.example.dinoenglish.repository.UserProgressRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Từ vựng và câu hỏi trắc nghiệm được lưu nhúng trong Lesson (vocabularies / quizQuestions),
 * nên thêm / xóa chúng chính là cập nhật document Lesson.
 */
@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final CourseService courseService;
    private final GamificationService gamificationService;

    public LessonService(LessonRepository lessonRepository,
                         CourseRepository courseRepository,
                         UserProgressRepository userProgressRepository,
                         QuizSubmissionRepository quizSubmissionRepository,
                         CourseService courseService,
                         GamificationService gamificationService) {
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userProgressRepository = userProgressRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.courseService = courseService;
        this.gamificationService = gamificationService;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }

    /** Nạp Lesson.course (@Transient) từ courseId để giao diện dùng lesson.course như trước. */
    private void attachCourse(Lesson lesson) {
        if (lesson != null && hasId(lesson.getCourseId())) {
            courseRepository.findById(lesson.getCourseId()).ifPresent(lesson::setCourse);
        }
    }

    public List<Lesson> getLessonsByCourse(Course course) {
        List<Lesson> lessons = lessonRepository.findByCourseIdAndActiveTrueOrderByOrderIndexAsc(course.getId());
        for (Lesson l : lessons) l.setCourse(course);
        return lessons;
    }

    public List<Lesson> getLessonsByCourseId(String courseId) {
        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        Course course = hasId(courseId) ? courseRepository.findById(courseId).orElse(null) : null;
        for (Lesson l : lessons) l.setCourse(course);
        return lessons;
    }

    public Optional<Lesson> getLessonById(String id) {
        if (!hasId(id)) return Optional.empty();
        Optional<Lesson> opt = lessonRepository.findById(id);
        opt.ifPresent(this::attachCourse);
        return opt;
    }

    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    /**
     * Lưu bài học từ form: nếu đã có id thì nạp bản ghi cũ từ DB và chỉ chép các trường cho phép sửa,
     * giữ nguyên từ vựng / câu hỏi quiz (save() của MongoDB ghi đè cả document nên không được lưu thẳng object từ form).
     */
    public Lesson saveLessonFromForm(Lesson form, Course course) {
        Lesson target;
        if (hasId(form.getId())) {
            target = lessonRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        } else {
            target = new Lesson();
        }
        target.setCourseId(course.getId());
        target.setTitle(form.getTitle());
        target.setDescription(form.getDescription());
        target.setLessonType(form.getLessonType());
        target.setOrderIndex(form.getOrderIndex());
        target.setXpReward(form.getXpReward());
        target.setContent(form.getContent());
        target.setAudioUrl(form.getAudioUrl());
        target.setVideoUrl(form.getVideoUrl());
        target.setActive(form.getActive() != null ? form.getActive() : Boolean.FALSE);
        Lesson saved = lessonRepository.save(target);
        saved.setCourse(course);
        return saved;
    }

    public boolean hasQuiz(Lesson lesson) {
        return lesson.getLessonType() == LessonType.QUIZ
                || (lesson.getQuizQuestions() != null && !lesson.getQuizQuestions().isEmpty());
    }

    /**
     * MongoDB không có khóa ngoại nên tự kiểm tra: bài học đã có học viên học / nộp bài thì không cho xóa
     * (ManagerController hiển thị gợi ý "hãy ẩn bài học").
     */
    public void deleteLesson(String id) {
        if (!hasId(id)) return;
        if (userProgressRepository.existsByLessonId(id) || quizSubmissionRepository.existsByLessonId(id)) {
            throw new DataIntegrityViolationException("Bài học đã có học viên học hoặc nộp bài");
        }
        lessonRepository.deleteById(id);
    }

    public boolean isLessonCompleted(String userId, String lessonId) {
        return userProgressRepository.existsByUserIdAndLessonId(userId, lessonId);
    }

    public Set<String> getCompletedLessonIds(String userId) {
        Set<String> ids = new HashSet<>();
        for (UserProgress up : userProgressRepository.findByUserId(userId)) {
            if (Boolean.TRUE.equals(up.getCompleted())) {
                ids.add(up.getLessonId());
            }
        }
        return ids;
    }

    public boolean markLessonCompleted(User user, Lesson lesson) {
        Optional<UserProgress> existing = userProgressRepository.findByUserIdAndLessonId(user.getId(), lesson.getId());
        if (existing.isPresent()) {
            return false; // already completed previously
        }

        UserProgress progress = UserProgress.builder()
                .userId(user.getId())
                .lessonId(lesson.getId())
                .courseId(lesson.getCourseId())
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
        try {
            userProgressRepository.save(progress);
        } catch (DuplicateKeyException e) {
            return false; // bấm hoàn thành 2 lần liên tiếp: unique index (userId, lessonId) chặn lần thứ hai
        }

        // Update gamification (XP, Streak, Level, Badges)
        int xp = lesson.getXpReward() != null ? lesson.getXpReward() : 20;
        gamificationService.recordStudyActivity(user, xp);

        // Update Course enrollment progress percentage
        if (hasId(lesson.getCourseId())) {
            courseService.updateCourseProgress(user.getId(), lesson.getCourseId());
        }

        return true;
    }

    // ---------------- Từ vựng (nhúng trong Lesson) ----------------

    public List<Vocabulary> getVocabulariesByLessonId(String lessonId) {
        return lessonRepository.findById(lessonId)
                .map(l -> l.getVocabularies() != null ? l.getVocabularies() : new ArrayList<Vocabulary>())
                .orElseGet(ArrayList::new);
    }

    public Vocabulary addVocabulary(String lessonId, Vocabulary vocabulary) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        if (!hasId(vocabulary.getId())) vocabulary.setId(UUID.randomUUID().toString());
        if (lesson.getVocabularies() == null) lesson.setVocabularies(new ArrayList<>());
        lesson.getVocabularies().add(vocabulary);
        lessonRepository.save(lesson);
        return vocabulary;
    }

    public void deleteVocabulary(String lessonId, String vocabularyId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        if (lesson.getVocabularies() != null
                && lesson.getVocabularies().removeIf(v -> Objects.equals(v.getId(), vocabularyId))) {
            lessonRepository.save(lesson);
        }
    }

    public long countTotalLessons() {
        return lessonRepository.count();
    }

    public long countActiveLessons() {
        return lessonRepository.countByActiveTrue();
    }
}
