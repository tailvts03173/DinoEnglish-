package com.example.dinoenglish.controller;

import com.example.dinoenglish.model.Role;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.service.CourseService;
import com.example.dinoenglish.service.LessonService;
import com.example.dinoenglish.service.QuizService;
import com.example.dinoenglish.service.UserService;
import com.example.dinoenglish.config.AuthInterceptor;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final QuizService quizService;

    public AdminController(UserService userService,
                           CourseService courseService,
                           LessonService lessonService,
                           QuizService quizService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.quizService = quizService;
    }

    private Long actorId(HttpSession session) {
        User actor = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        return actor != null ? actor.getId() : null;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("totalLearners", userService.countUsersByRole(Role.USER));
        model.addAttribute("totalManagers", userService.countUsersByRole(Role.MANAGER));
        model.addAttribute("totalCourses", courseService.countTotalCourses());
        model.addAttribute("totalLessons", lessonService.countTotalLessons());
        model.addAttribute("totalSubmissions", quizService.countTotalSubmissions());

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/create")
    public String createUser(@RequestParam("username") String username,
                             @RequestParam("fullName") String fullName,
                             @RequestParam("email") String email,
                             @RequestParam("phone") String phone,
                             @RequestParam("password") String password,
                             @RequestParam("role") String roleStr,
                             RedirectAttributes redirectAttributes) {
        try {
            Role role = Role.valueOf(roleStr);
            userService.createUserWithRole(username, password, fullName, email, phone, role);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản mới thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo tài khoản. Vui lòng kiểm tra lại thông tin.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.toggleUserStatus(id, actorId(session));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật trạng thái của " + user.getUsername() + " thành: " + user.getStatus().getDisplayName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thực hiện thao tác. Vui lòng thử lại.");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable("id") Long id,
                                 @RequestParam("role") String roleStr,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        try {
            Role newRole = Role.valueOf(roleStr);
            User user = userService.changeUserRole(id, newRole, actorId(session));
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã cập nhật vai trò của " + user.getUsername() + " thành: " + newRole.getDisplayName());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thực hiện thao tác. Vui lòng thử lại.");
        }
        return "redirect:/admin/users";
    }
}
