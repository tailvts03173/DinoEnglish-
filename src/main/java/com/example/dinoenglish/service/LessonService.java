package com.example.dinoenglish.service;

import com.example.dinoenglish.model.*;
import com.example.dinoenglish.repository.LessonRepository;
import com.example.dinoenglish.repository.QuizQuestionRepository;
import com.example.dinoenglish.repository.UserProgressRepository;
import com.example.dinoenglish.repository.VocabularyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final UserProgressRepository userProgressRepository;
    private final CourseService courseService;
    private final GamificationService gamificationService;

    public LessonService(LessonRepository lessonRepository,
                         VocabularyRepository vocabularyRepository,
                         QuizQuestionRepository quizQuestionRepository,
                         UserProgressRepository userProgressRepository,
                         CourseService courseService,
                         GamificationService gamificationService) {
        this.lessonRepository = lessonRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.userProgressRepository = userProgressRepository;
        this.courseService = courseService;
        this.gamificationService = gamificationService;
    }

    public List<Lesson> getLessonsByCourse(Course course) {
        return lessonRepository.findByCourseAndActiveTrueOrderByOrderIndexAsc(course);
    }

    public List<Lesson> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    @Transactional
    public Lesson saveLesson(Lesson lesson) {
        return lessonRepository.save(lesson);
    }

    /**
     * Lưu bài học từ form: nếu đã có id thì nạp bản ghi cũ từ DB và chỉ chép các trường
     * cho phép sửa, giữ nguyên từ vựng / câu hỏi quiz (tránh orphanRemoval xóa sạch dữ liệu con).
     */
    @Transactional
    public Lesson saveLessonFromForm(Lesson form, Course course) {
        Lesson target;
        if (form.getId() != null) {
            target = lessonRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        } else {
            target = new Lesson();
        }
        target.setCourse(course);
        target.setTitle(form.getTitle());
        target.setDescription(form.getDescription());
        target.setLessonType(form.getLessonType());
        target.setOrderIndex(form.getOrderIndex());
        target.setXpReward(form.getXpReward());
        target.setContent(form.getContent());
        target.setAudioUrl(form.getAudioUrl());
        target.setVideoUrl(form.getVideoUrl());
        target.setActive(form.getActive() != null ? form.getActive() : Boolean.FALSE);
        return lessonRepository.save(target);
    }

    public boolean hasQuiz(Lesson lesson) {
        return lesson.getLessonType() == LessonType.QUIZ
                || quizQuestionRepository.countByLessonId(lesson.getId()) > 0;
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    public boolean isLessonCompleted(Long userId, Long lessonId) {
        return userProgressRepository.existsByUserIdAndLessonId(userId, lessonId);
    }

    public Set<Long> getCompletedLessonIds(Long userId) {
        List<UserProgress> list = userProgressRepository.findByUserId(userId);
        Set<Long> ids = new HashSet<>();
        for (UserProgress up : list) {
            if (up.getCompleted() != null && up.getCompleted()) {
                ids.add(up.getLesson().getId());
            }
        }
        return ids;
    }

    @Transactional
    public boolean markLessonCompleted(User user, Lesson lesson) {
        Optional<UserProgress> existing = userProgressRepository.findByUserAndLesson(user, lesson);
        if (existing.isPresent()) {
            return false; // already completed previously
        }

        UserProgress progress = UserProgress.builder()
                .user(user)
                .lesson(lesson)
                .completed(true)
                .completedAt(LocalDateTime.now())
                .build();
        userProgressRepository.save(progress);

        // Update gamification (XP, Streak, Level, Badges)
        int xp = lesson.getXpReward() != null ? lesson.getXpReward() : 20;
        gamificationService.recordStudyActivity(user, xp);

        // Update Course enrollment progress percentage
        if (lesson.getCourse() != null) {
            courseService.updateCourseProgress(user.getId(), lesson.getCourse().getId());
        }

        return true;
    }

    public List<Vocabulary> getVocabulariesByLessonId(Long lessonId) {
        return vocabularyRepository.findByLessonId(lessonId);
    }

    @Transactional
    public Vocabulary saveVocabulary(Vocabulary vocabulary) {
        return vocabularyRepository.save(vocabulary);
    }

    @Transactional
    public void deleteVocabulary(Long id) {
        vocabularyRepository.deleteById(id);
    }

    public long countTotalLessons() {
        return lessonRepository.count();
    }

    public long countActiveLessons() {
        return lessonRepository.countByActiveTrue();
    }
}
