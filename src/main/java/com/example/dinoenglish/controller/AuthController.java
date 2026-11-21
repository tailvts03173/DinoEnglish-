package com.example.dinoenglish.controller;

import com.example.dinoenglish.config.AuthInterceptor;
import com.example.dinoenglish.model.Role;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserStatus;
import com.example.dinoenglish.service.UserService;
import com.example.dinoenglish.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "message", required = false) String message,
                                HttpSession session,
                                Model model) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            return redirectByRole(currentUser);
        }

        if ("unauthorized".equals(error)) {
            model.addAttribute("errorMessage", "Bạn cần đăng nhập với quyền phù hợp để truy cập trang này!");
        } else if ("blocked".equals(error)) {
            model.addAttribute("errorMessage", "Tài khoản của bạn đã bị tạm khóa. Vui lòng liên hệ Admin!");
        } else if ("invalid".equals(error)) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }

        if ("require_login".equals(message)) {
            model.addAttribute("infoMessage", "Vui lòng đăng nhập để tiếp tục hành trình học cùng Dino!");
        } else if ("logged_out".equals(message)) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công. Hẹn gặp lại bạn!");
        } else if ("password_reset".equals(message)) {
            model.addAttribute("successMessage", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
        } else if ("registered".equals(message)) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập để bắt đầu.");
        }

        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("usernameOrEmail") String usernameOrEmail,
                               @RequestParam("password") String password,
                               HttpServletRequest request,
                               Model model) {
        HttpSession session = request.getSession();
        Optional<User> userOpt = userService.authenticate(usernameOrEmail, password);

        if (userOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác!");
            model.addAttribute("enteredUsername", usernameOrEmail);
            return "auth/login";
        }

        User user = userOpt.get();

        if (user.getStatus() == UserStatus.BLOCKED) {
            model.addAttribute("errorMessage", "Tài khoản này hiện đang bị khóa!");
            return "auth/login";
        }

        // Chống session fixation: cấp session ID mới sau khi đăng nhập thành công
        request.changeSessionId();
        session = request.getSession();
        session.setAttribute(AuthInterceptor.SESSION_USER, user);

        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
        if (redirectUrl != null && !redirectUrl.isBlank()) {
            session.removeAttribute("redirectAfterLogin");
            // Chỉ chuyển hướng tới đường dẫn nội bộ (chống open redirect)
            if (redirectUrl.startsWith("/") && !redirectUrl.startsWith("//") && !redirectUrl.contains("\\")) {
                return "redirect:" + redirectUrl;
            }
        }

        return redirectByRole(user);
    }

    @GetMapping("/register")
    public String showRegisterForm(HttpSession session) {
        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (currentUser != null) {
            return redirectByRole(currentUser);
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam("phone") String phone,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "auth/register";
        }

        try {
            userService.register(username, password, fullName, email, phone);
            return "redirect:/login?message=registered";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("fullName", fullName);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "auth/register";
        }
    }

    // ============ Quên mật khẩu: gửi mã qua email ============
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("step", 1);
        return "auth/forgot-password";
    }

    /** Bước 1: nhận email, gửi mã 6 số. Luôn trả về cùng một thông báo để không lộ email nào đã đăng ký. */
    @PostMapping("/forgot-password")
    public String sendResetCode(@RequestParam("email") String email, Model model) {
        passwordResetService.requestCode(email);
        model.addAttribute("step", 2);
        model.addAttribute("email", email.trim());
        model.addAttribute("infoMessage", "Nếu email này đã đăng ký, Dino vừa gửi một mã xác minh 6 số tới hộp thư của bạn. "
                + "Mã có hiệu lực " + PasswordResetService.VALID_MINUTES + " phút (kiểm tra cả mục Spam).");
        return "auth/forgot-password";
    }

    /** Bước 2: nhận mã + mật khẩu mới. */
    @PostMapping("/forgot-password/reset")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("code") String code,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        model.addAttribute("step", 2);
        model.addAttribute("email", email.trim());

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu xác nhận không khớp!");
            return "auth/forgot-password";
        }
        try {
            UserService.validatePassword(newPassword);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        }

        PasswordResetService.Result result = passwordResetService.resetPassword(email, code, newPassword);
        switch (result) {
            case OK:
                return "redirect:/login?message=password_reset";
            case TOO_MANY_ATTEMPTS:
                model.addAttribute("step", 1);
                model.addAttribute("errorMessage", "Bạn đã nhập sai mã quá nhiều lần. Vui lòng yêu cầu mã mới.");
                break;
            default:
                model.addAttribute("errorMessage", "Mã xác minh không đúng hoặc đã hết hạn. Vui lòng kiểm tra lại hoặc yêu cầu mã mới.");
        }
        return "auth/forgot-password";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?message=logged_out";
    }

    private String redirectByRole(User user) {
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else if (user.getRole() == Role.MANAGER) {
            return "redirect:/manager/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }
}
