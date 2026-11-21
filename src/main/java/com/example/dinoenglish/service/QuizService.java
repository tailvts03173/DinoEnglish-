package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.QuizQuestion;
import com.example.dinoenglish.model.QuizSubmission;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.repository.LessonRepository;
import com.example.dinoenglish.repository.QuizSubmissionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/** Câu hỏi trắc nghiệm được lưu nhúng trong Lesson.quizQuestions. */
@Service
public class QuizService {

    private final LessonRepository lessonRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final LessonService lessonService;
    private final GamificationService gamificationService;

    public QuizService(LessonRepository lessonRepository,
                       QuizSubmissionRepository quizSubmissionRepository,
                       LessonService lessonService,
                       GamificationService gamificationService) {
        this.lessonRepository = lessonRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.lessonService = lessonService;
        this.gamificationService = gamificationService;
    }

    public List<QuizQuestion> getQuestionsByLessonId(String lessonId) {
        if (lessonId == null || lessonId.isBlank()) return new ArrayList<>();
        List<QuizQuestion> list = lessonRepository.findById(lessonId)
                .map(l -> l.getQuizQuestions() != null ? new ArrayList<>(l.getQuizQuestions()) : new ArrayList<QuizQuestion>())
                .orElseGet(ArrayList::new);
        list.sort(Comparator.comparing(QuizQuestion::getOrderIndex, Comparator.nullsLast(Comparator.<Integer>naturalOrder())));
        return list;
    }

    public QuizQuestion addQuestion(String lessonId, QuizQuestion question) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        if (question.getId() == null || question.getId().isBlank()) question.setId(UUID.randomUUID().toString());
        if (lesson.getQuizQuestions() == null) lesson.setQuizQuestions(new ArrayList<>());
        if (question.getOrderIndex() == null) question.setOrderIndex(lesson.getQuizQuestions().size() + 1);
        lesson.getQuizQuestions().add(question);
        lessonRepository.save(lesson);
        return question;
    }

    public void deleteQuestion(String lessonId, String questionId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học!"));
        if (lesson.getQuizQuestions() != null
                && lesson.getQuizQuestions().removeIf(q -> Objects.equals(q.getId(), questionId))) {
            lessonRepository.save(lesson);
        }
    }

    public QuizSubmission gradeAndSubmit(User user, Lesson lesson, Map<String, String> answers) {
        List<QuizQuestion> questions = getQuestionsByLessonId(lesson.getId());
        int totalQuestions = questions.size();
        int correctCount = 0;

        for (QuizQuestion q : questions) {
            String selected = answers.get(q.getId());
            if (selected != null && selected.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                correctCount++;
            }
        }

        int score = totalQuestions > 0 ? (correctCount * 100) / totalQuestions : 0;
        boolean passed = score >= 60;
        // XP hiển thị = XP thực nhận: chỉ lần đạt đầu tiên mới nhận XP của bài học.
        // Trượt hoặc làm lại bài đã hoàn thành không được cộng XP (chống cày XP).
        boolean alreadyCompleted = lessonService.isLessonCompleted(user.getId(), lesson.getId());
        int xpEarned = (passed && !alreadyCompleted)
                ? (lesson.getXpReward() != null ? lesson.getXpReward() : 20)
                : 0;

        QuizSubmission submission = QuizSubmission.builder()
                .userId(user.getId())
                .lessonId(lesson.getId())
                .lesson(lesson)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .score(score)
                .passed(passed)
                .xpEarned(xpEarned)
                .build();

        QuizSubmission saved = quizSubmissionRepository.save(submission);
        saved.setLesson(lesson);

        // If passed, mark lesson as completed
        if (passed && !alreadyCompleted) {
            lessonService.markLessonCompleted(user, lesson);
        } else {
            // Vẫn tính hoạt động học trong ngày (streak) nhưng không cộng XP
            gamificationService.recordStudyActivity(user, 0);
        }

        // Special Badge for 100% quiz score
        if (score == 100 && totalQuestions >= 3) {
            gamificationService.awardBadgeIfEligible(user, "QUIZ_ACE");
        }

        return saved;
    }

    public List<QuizSubmission> getUserSubmissions(String userId) {
        List<QuizSubmission> list = new ArrayList<>(quizSubmissionRepository.findByUserIdOrderBySubmittedAtDesc(userId));
        Set<String> lessonIds = new HashSet<>();
        for (QuizSubmission s : list) lessonIds.add(s.getLessonId());
        Map<String, Lesson> byId = new HashMap<>();
        for (Lesson l : lessonRepository.findAllById(lessonIds)) byId.put(l.getId(), l);
        for (QuizSubmission s : list) s.setLesson(byId.get(s.getLessonId())); // nạp QuizSubmission.lesson (@Transient)
        list.removeIf(s -> s.getLesson() == null);
        return list;
    }

    public long countTotalSubmissions() {
        return quizSubmissionRepository.count();
    }
}
