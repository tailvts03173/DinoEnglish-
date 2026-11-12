package com.example.dinoenglish.controller;

import com.example.dinoenglish.config.AuthInterceptor;
import com.example.dinoenglish.model.*;
import com.example.dinoenglish.service.*;
import com.example.dinoenglish.util.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserLearningController {

    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final QuizService quizService;
    private final GamificationService gamificationService;
    private final DocumentService documentService;

    public UserLearningController(UserService userService,
                                  CourseService courseService,
                                  LessonService lessonService,
                                  QuizService quizService,
                                  GamificationService gamificationService,
                                  DocumentService documentService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.quizService = quizService;
        this.gamificationService = gamificationService;
        this.documentService = documentService;
    }

    private User getFreshUser(HttpSession session) {
        User sessionUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (sessionUser == null) return null;
        gamificationService.resetBrokenStreaks();
        User fresh = userService.findById(sessionUser.getId()).orElse(sessionUser);
        session.setAttribute(AuthInterceptor.SESSION_USER, fresh);
        return fresh;
    }

    /** Khóa học hoặc bài học bị ẩn thì học viên không được truy cập. */
    private boolean isAccessible(Lesson lesson) {
        return lesson != null
                && !Boolean.FALSE.equals(lesson.getActive())
                && lesson.getCourse() != null
                && !Boolean.FALSE.equals(lesson.getCourse().getActive());
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = getFreshUser(session);
        if (user == null) return "redirect:/login";

        List<Enrollment> enrollments = courseService.getUserEnrollments(user.getId());
        List<QuizSubmission> recentQuizzes = quizService.getUserSubmissions(user.getId());
        if (recentQuizzes.size() > 5) {
            recentQuizzes = recentQuizzes.subList(0, 5);
        }

        List<UserBadge> userBadges = gamificationService.getUserBadges(user);
        List<User> topLearners = gamificationService.getTopLearners(5);

        model.addAttribute("user", user);
        model.addAttribute("enrollments", enrollments);
        model.addAttribute("recentQuizzes", recentQuizzes);
        model.addAttribute("userBadges", userBadges);
        model.addAttribute("topLearners", topLearners);

        return "user/dashboard";
    }

    @GetMapping("/courses")
    public String myCourses(HttpSession session, Model model) {
        User user = getFreshUser(session);
        List<Enrollment> enrollments = courseService.getUserEnrollments(user.getId());
        model.addAttribute("enrollments", enrollments);
        return "user/my-courses";
    }

    @PostMapping("/enroll/{courseId}")
    public String enroll(@PathVariable("courseId") Long courseId,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        User user = getFreshUser(session);
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isPresent() && !Boolean.FALSE.equals(courseOpt.get().getActive())) {
            Course course = courseOpt.get();
            courseService.enrollUser(user, course);
            redirectAttributes.addFlashAttribute("successMessage", "Chúc mừng bạn đã đăng ký khóa học: " + course.getTitle() + "! Hãy bắt đầu học ngay cùng Dino.");
            return "redirect:/courses/" + course.getSlug();
        }
        return "redirect:/courses";
    }

    @GetMapping("/lesson/{lessonId}")
    public String studyLesson(@PathVariable("lessonId") Long lessonId,
                              HttpSession session,
                              Model model) {
        User user = getFreshUser(session);
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty() || !isAccessible(lessonOpt.get())) {
            return "redirect:/user/dashboard";
        }

        Lesson lesson = lessonOpt.get();
        Course course = lesson.getCourse();

        // Ensure user is enrolled
        if (!courseService.isUserEnrolled(user.getId(), course.getId())) {
            courseService.enrollUser(user, course);
        }

        List<Lesson> courseLessons = lessonService.getLessonsByCourse(course);
        Set<Long> completedIds = lessonService.getCompletedLessonIds(user.getId());
        boolean isCompleted = completedIds.contains(lesson.getId());

        // Find prev and next lesson
        Lesson prevLesson = null;
        Lesson nextLesson = null;
        for (int i = 0; i < courseLessons.size(); i++) {
            if (courseLessons.get(i).getId().equals(lesson.getId())) {
                if (i > 0) prevLesson = courseLessons.get(i - 1);
                if (i < courseLessons.size() - 1) nextLesson = courseLessons.get(i + 1);
                break;
            }
        }

        List<Vocabulary> vocabularies = lessonService.getVocabulariesByLessonId(lesson.getId());

        model.addAttribute("user", user);
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        model.addAttribute("courseLessons", courseLessons);
        model.addAttribute("completedIds", completedIds);
        model.addAttribute("isCompleted", isCompleted);
        model.addAttribute("prevLesson", prevLesson);
        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("vocabularies", vocabularies);

        return "user/lesson-study";
    }

    @PostMapping("/lesson/{lessonId}/complete")
    public String completeLesson(@PathVariable("lessonId") Long lessonId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getFreshUser(session);
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isPresent() && isAccessible(lessonOpt.get())) {
            Lesson lesson = lessonOpt.get();
            // Bài kiểm tra / bài có câu hỏi trắc nghiệm chỉ hoàn thành khi đạt quiz, không được bỏ qua bằng nút này
            if (lessonService.hasQuiz(lesson)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bài học này có phần trắc nghiệm. Hãy làm bài kiểm tra và đạt từ 60% để hoàn thành!");
                return "redirect:/user/lesson/" + lessonId;
            }
            boolean awarded = lessonService.markLessonCompleted(user, lesson);
            if (awarded) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Tuyệt vời! Bạn đã hoàn thành bài học và nhận được +" + lesson.getXpReward() + " XP! 🔥");
            }
            return "redirect:/user/lesson/" + lessonId;
        }
        return "redirect:/user/dashboard";
    }

    @GetMapping("/quiz/{lessonId}")
    public String takeQuiz(@PathVariable("lessonId") Long lessonId,
                           HttpSession session,
                           Model model) {
        User user = getFreshUser(session);
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty() || !isAccessible(lessonOpt.get())) {
            return "redirect:/user/dashboard";
        }

        Lesson lesson = lessonOpt.get();
        List<QuizQuestion> questions = quizService.getQuestionsByLessonId(lessonId);

        model.addAttribute("user", user);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", lesson.getCourse());
        model.addAttribute("questions", questions);

        return "user/quiz";
    }

    @PostMapping("/quiz/{lessonId}/submit")
    public String submitQuiz(@PathVariable("lessonId") Long lessonId,
                             HttpServletRequest request,
                             HttpSession session,
                             Model model) {
        User user = getFreshUser(session);
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty() || !isAccessible(lessonOpt.get())) {
            return "redirect:/user/dashboard";
        }

        Lesson lesson = lessonOpt.get();
        List<QuizQuestion> questions = quizService.getQuestionsByLessonId(lessonId);

        Map<Long, String> answers = new HashMap<>();
        for (QuizQuestion q : questions) {
            String ans = request.getParameter("question_" + q.getId());
            if (ans != null) {
                answers.put(q.getId(), ans);
            }
        }

        QuizSubmission submission = quizService.gradeAndSubmit(user, lesson, answers);

        model.addAttribute("user", getFreshUser(session));
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", lesson.getCourse());
        model.addAttribute("questions", questions);
        model.addAttribute("answers", answers);
        model.addAttribute("submission", submission);

        return "user/quiz-result";
    }

    @GetMapping("/streak")
    public String streakHub(HttpSession session, Model model) {
        User user = getFreshUser(session);
        List<UserBadge> userBadges = gamificationService.getUserBadges(user);
        List<Badge> allBadges = gamificationService.getAllBadges();
        List<User> topStreaks = gamificationService.getTopStreaks(10);

        model.addAttribute("user", user);
        model.addAttribute("userBadges", userBadges);
        model.addAttribute("allBadges", allBadges);
        model.addAttribute("topStreaks", topStreaks);

        return "user/streak";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(HttpSession session, Model model) {
        User user = getFreshUser(session);
        List<User> topByXp = gamificationService.getTopLearners(20);
        List<User> topByStreak = gamificationService.getTopStreaks(20);

        model.addAttribute("user", user);
        model.addAttribute("topByXp", topByXp);
        model.addAttribute("topByStreak", topByStreak);

        return "user/leaderboard";
    }

    @GetMapping("/documents")
    public String documents(HttpSession session, Model model) {
        User user = getFreshUser(session);
        List<DocumentResource> documents = documentService.getAllDocuments();
        model.addAttribute("user", user);
        model.addAttribute("documents", documents);
        return "user/documents";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = getFreshUser(session);
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam("phone") String phone,
                                @RequestParam("avatarUrl") String avatarUrl,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = getFreshUser(session);
        try {
            userService.updateProfile(user.getId(), fullName, phone, UrlUtil.safeUrl(avatarUrl));
            getFreshUser(session); // refresh session
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin cá nhân thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = getFreshUser(session);
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới xác nhận không khớp!");
            return "redirect:/user/profile";
        }

        try {
            boolean ok = userService.changePassword(user.getId(), currentPassword, newPassword);
            if (ok) {
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu hiện tại không chính xác!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/profile";
    }
}
