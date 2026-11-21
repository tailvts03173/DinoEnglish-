package com.example.dinoenglish.controller;

import com.example.dinoenglish.config.AuthInterceptor;
import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.CourseLevel;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.service.CourseService;
import com.example.dinoenglish.service.LessonService;
import com.example.dinoenglish.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserService userService;

    public HomeController(CourseService courseService,
                          LessonService lessonService,
                          UserService userService) {
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        List<Course> courses = courseService.getAllActiveCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("totalCourses", courseService.countActiveCourses());
        model.addAttribute("totalLessons", lessonService.countActiveLessons());
        model.addAttribute("totalUsers", userService.countAllUsers());
        return "home";
    }

    @GetMapping("/courses")
    public String listCourses(@RequestParam(value = "level", required = false) String levelStr, Model model) {
        List<Course> courses;
        if (levelStr != null && !levelStr.isBlank()) {
            try {
                CourseLevel level = CourseLevel.valueOf(levelStr.toUpperCase());
                courses = courseService.getCoursesByLevel(level);
                model.addAttribute("selectedLevel", levelStr);
            } catch (IllegalArgumentException e) {
                courses = courseService.getAllActiveCourses();
            }
        } else {
            courses = courseService.getAllActiveCourses();
        }

        model.addAttribute("courses", courses);
        model.addAttribute("levels", CourseLevel.values());
        return "courses";
    }

    @GetMapping("/courses/{slug}")
    public String courseDetail(@PathVariable("slug") String slug, HttpSession session, Model model) {
        Optional<Course> courseOpt = courseService.getCourseBySlug(slug);
        if (courseOpt.isEmpty() || Boolean.FALSE.equals(courseOpt.get().getActive())) {
            return "redirect:/courses";
        }

        Course course = courseOpt.get();
        model.addAttribute("course", course);
        model.addAttribute("lessons", lessonService.getLessonsByCourse(course));

        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        boolean isEnrolled = false;
        if (currentUser != null) {
            isEnrolled = courseService.isUserEnrolled(currentUser.getId(), course.getId());
            model.addAttribute("completedLessonIds", lessonService.getCompletedLessonIds(currentUser.getId()));
        }
        model.addAttribute("isEnrolled", isEnrolled);

        return "course-detail";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
