package com.example.dinoenglish.controller;

import com.example.dinoenglish.model.*;
import com.example.dinoenglish.service.CourseService;
import com.example.dinoenglish.service.DocumentService;
import com.example.dinoenglish.service.LessonService;
import com.example.dinoenglish.service.QuizService;
import com.example.dinoenglish.util.UrlUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final QuizService quizService;
    private final DocumentService documentService;

    public ManagerController(CourseService courseService,
                             LessonService lessonService,
                             QuizService quizService,
                             DocumentService documentService) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.quizService = quizService;
        this.documentService = documentService;
    }

    /** Thông báo lỗi thân thiện: không để lộ lỗi thô của cơ sở dữ liệu ra giao diện. */
    private String friendlyError(Exception e, String deleteHint) {
        if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        }
        if (e instanceof DataIntegrityViolationException) {
            return deleteHint != null
                    ? deleteHint
                    : "Dữ liệu không hợp lệ hoặc bị trùng lặp (ví dụ slug đã tồn tại). Vui lòng kiểm tra lại.";
        }
        return "Đã xảy ra lỗi khi xử lý. Vui lòng thử lại.";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalCourses", courseService.countTotalCourses());
        model.addAttribute("totalLessons", lessonService.countTotalLessons());
        model.addAttribute("totalDocuments", documentService.countTotalDocuments());
        model.addAttribute("courses", courseService.getAllCourses());
        return "manager/dashboard";
    }

    // ================= Courses =================
    @GetMapping("/courses")
    public String listCourses(Model model) {
        model.addAttribute("courses", courseService.getAllCourses());
        return "manager/courses";
    }

    @GetMapping("/courses/new")
    public String newCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("isNew", true);
        return "manager/course-form";
    }

    @PostMapping("/courses/save")
    public String saveCourse(@ModelAttribute("course") Course course,
                             RedirectAttributes redirectAttributes) {
        try {
            courseService.saveCourseFromForm(course);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu khóa học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/courses";
    }

    @GetMapping("/courses/{id}/edit")
    public String editCourseForm(@PathVariable("id") Long id, Model model) {
        Optional<Course> courseOpt = courseService.getCourseById(id);
        if (courseOpt.isEmpty()) {
            return "redirect:/manager/courses";
        }
        model.addAttribute("course", courseOpt.get());
        model.addAttribute("levels", CourseLevel.values());
        model.addAttribute("isNew", false);
        return "manager/course-form";
    }

    @PostMapping("/courses/{id}/delete")
    public String deleteCourse(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khóa học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa: " + friendlyError(e,
                    "Không thể xóa khóa học đã có học viên đăng ký hoặc học. Hãy tắt trạng thái \"Hiển thị\" để ẩn khóa học thay vì xóa."));
        }
        return "redirect:/manager/courses";
    }

    // ================= Lessons =================
    @GetMapping("/courses/{courseId}/lessons")
    public String manageCourseLessons(@PathVariable("courseId") Long courseId, Model model) {
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) {
            return "redirect:/manager/courses";
        }
        Course course = courseOpt.get();
        List<Lesson> lessons = lessonService.getLessonsByCourseId(courseId);

        model.addAttribute("course", course);
        model.addAttribute("lessons", lessons);
        model.addAttribute("lessonTypes", LessonType.values());
        return "manager/lessons";
    }

    @GetMapping("/courses/{courseId}/lessons/new")
    public String newLessonForm(@PathVariable("courseId") Long courseId, Model model) {
        Optional<Course> courseOpt = courseService.getCourseById(courseId);
        if (courseOpt.isEmpty()) return "redirect:/manager/courses";

        Lesson lesson = new Lesson();
        lesson.setCourse(courseOpt.get());
        lesson.setOrderIndex(lessonService.getLessonsByCourseId(courseId).size() + 1);

        model.addAttribute("course", courseOpt.get());
        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("isNew", true);
        return "manager/lesson-form";
    }

    @PostMapping("/lessons/save")
    public String saveLesson(@ModelAttribute("lesson") Lesson lesson,
                             @RequestParam("courseId") Long courseId,
                             RedirectAttributes redirectAttributes) {
        try {
            Course course = courseService.getCourseById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học!"));
            lesson.setAudioUrl(UrlUtil.safeUrl(lesson.getAudioUrl()));
            lesson.setVideoUrl(UrlUtil.safeUrl(lesson.getVideoUrl()));
            lessonService.saveLessonFromForm(lesson, course);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu bài học thành công!");
            return "redirect:/manager/courses/" + courseId + "/lessons";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
            return "redirect:/manager/courses/" + courseId + "/lessons";
        }
    }

    @GetMapping("/lessons/{id}/edit")
    public String editLessonForm(@PathVariable("id") Long id, Model model) {
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) return "redirect:/manager/courses";

        Lesson lesson = lessonOpt.get();
        model.addAttribute("course", lesson.getCourse());
        model.addAttribute("lesson", lesson);
        model.addAttribute("lessonTypes", LessonType.values());
        model.addAttribute("isNew", false);
        return "manager/lesson-form";
    }

    @PostMapping("/lessons/{id}/delete")
    public String deleteLesson(@PathVariable("id") Long id,
                               @RequestParam("courseId") Long courseId,
                               RedirectAttributes redirectAttributes) {
        try {
            lessonService.deleteLesson(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bài học thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e,
                    "Không thể xóa bài học đã có học viên học hoặc nộp bài. Hãy tắt trạng thái \"Hiển thị\" để ẩn bài học thay vì xóa."));
        }
        return "redirect:/manager/courses/" + courseId + "/lessons";
    }

    // ================= Vocabularies =================
    @GetMapping("/lessons/{lessonId}/vocabularies")
    public String manageVocabularies(@PathVariable("lessonId") Long lessonId, Model model) {
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty()) return "redirect:/manager/courses";

        Lesson lesson = lessonOpt.get();
        List<Vocabulary> vocabularies = lessonService.getVocabulariesByLessonId(lessonId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("course", lesson.getCourse());
        model.addAttribute("vocabularies", vocabularies);
        model.addAttribute("newVocab", new Vocabulary());
        return "manager/vocabularies";
    }

    @PostMapping("/lessons/{lessonId}/vocabularies/new")
    public String addVocabulary(@PathVariable("lessonId") Long lessonId,
                                @ModelAttribute("newVocab") Vocabulary vocab,
                                RedirectAttributes redirectAttributes) {
        try {
            Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
            if (lessonOpt.isPresent()) {
                vocab.setImageUrl(UrlUtil.safeUrl(vocab.getImageUrl()));
                vocab.setAudioUrl(UrlUtil.safeUrl(vocab.getAudioUrl()));
                vocab.setLesson(lessonOpt.get());
                lessonService.saveVocabulary(vocab);
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm từ vựng '" + vocab.getWord() + "' thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/lessons/" + lessonId + "/vocabularies";
    }

    @PostMapping("/vocabularies/{id}/delete")
    public String deleteVocabulary(@PathVariable("id") Long id,
                                   @RequestParam("lessonId") Long lessonId,
                                   RedirectAttributes redirectAttributes) {
        try {
            lessonService.deleteVocabulary(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa từ vựng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/lessons/" + lessonId + "/vocabularies";
    }

    // ================= Quizzes =================
    @GetMapping("/lessons/{lessonId}/quizzes")
    public String manageQuizzes(@PathVariable("lessonId") Long lessonId, Model model) {
        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty()) return "redirect:/manager/courses";

        Lesson lesson = lessonOpt.get();
        List<QuizQuestion> questions = quizService.getQuestionsByLessonId(lessonId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("course", lesson.getCourse());
        model.addAttribute("questions", questions);
        model.addAttribute("newQuestion", new QuizQuestion());
        return "manager/quizzes";
    }

    @PostMapping("/lessons/{lessonId}/quizzes/new")
    public String addQuizQuestion(@PathVariable("lessonId") Long lessonId,
                                  @ModelAttribute("newQuestion") QuizQuestion question,
                                  RedirectAttributes redirectAttributes) {
        try {
            Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
            if (lessonOpt.isPresent()) {
                question.setLesson(lessonOpt.get());
                quizService.saveQuestion(question);
                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm câu hỏi trắc nghiệm thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/lessons/" + lessonId + "/quizzes";
    }

    @PostMapping("/quizzes/{id}/delete")
    public String deleteQuizQuestion(@PathVariable("id") Long id,
                                     @RequestParam("lessonId") Long lessonId,
                                     RedirectAttributes redirectAttributes) {
        try {
            quizService.deleteQuestion(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa câu hỏi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/lessons/" + lessonId + "/quizzes";
    }

    // ================= Documents =================
    @GetMapping("/documents")
    public String manageDocuments(Model model) {
        model.addAttribute("documents", documentService.getAllDocuments());
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("newDoc", new DocumentResource());
        return "manager/documents";
    }

    @PostMapping("/documents/save")
    public String saveDocument(@ModelAttribute("newDoc") DocumentResource doc,
                               @RequestParam(value = "courseId", required = false) Long courseId,
                               RedirectAttributes redirectAttributes) {
        try {
            if (courseId != null) {
                courseService.getCourseById(courseId).ifPresent(doc::setCourse);
            }
            doc.setFileUrl(UrlUtil.safeUrl(doc.getFileUrl()));
            documentService.saveDocument(doc);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm tài liệu học tập thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/documents";
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            documentService.deleteDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + friendlyError(e, null));
        }
        return "redirect:/manager/documents";
    }
}
