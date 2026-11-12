package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.QuizQuestion;
import com.example.dinoenglish.model.QuizSubmission;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.repository.QuizQuestionRepository;
import com.example.dinoenglish.repository.QuizSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final LessonService lessonService;
    private final GamificationService gamificationService;

    public QuizService(QuizQuestionRepository quizQuestionRepository,
                       QuizSubmissionRepository quizSubmissionRepository,
                       LessonService lessonService,
                       GamificationService gamificationService) {
        this.quizQuestionRepository = quizQuestionRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.lessonService = lessonService;
        this.gamificationService = gamificationService;
    }

    public List<QuizQuestion> getQuestionsByLessonId(Long lessonId) {
        return quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);
    }

    public Optional<QuizQuestion> getQuestionById(Long id) {
        return quizQuestionRepository.findById(id);
    }

    @Transactional
    public QuizQuestion saveQuestion(QuizQuestion question) {
        return quizQuestionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        quizQuestionRepository.deleteById(id);
    }

    @Transactional
    public QuizSubmission gradeAndSubmit(User user, Lesson lesson, Map<Long, String> answers) {
        List<QuizQuestion> questions = quizQuestionRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
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
                .user(user)
                .lesson(lesson)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .score(score)
                .passed(passed)
                .xpEarned(xpEarned)
                .build();

        QuizSubmission saved = quizSubmissionRepository.save(submission);

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

    public List<QuizSubmission> getUserSubmissions(Long userId) {
        return quizSubmissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public long countTotalSubmissions() {
        return quizSubmissionRepository.count();
    }
}
