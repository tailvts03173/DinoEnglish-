package com.example.dinoenglish.config;

import com.example.dinoenglish.model.Role;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserStatus;
import com.example.dinoenglish.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER = "currentUser";

    private final UserRepository userRepository;

    public AuthInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Đường dẫn trong ứng dụng, đã bỏ context-path, ;jsessionid và gộp các dấu // liên tiếp. */
    static String normalizedPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        int semi = uri.indexOf(';');
        if (semi >= 0) uri = uri.substring(0, semi);
        uri = uri.replaceAll("/{2,}", "/");
        if (uri.length() > 1 && uri.endsWith("/")) uri = uri.substring(0, uri.length() - 1);
        return uri.isEmpty() ? "/" : uri;
    }

    private static boolean isPublic(String uri) {
        return uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.startsWith("/fonts/") || uri.equals("/favicon.ico")
                || uri.equals("/") || uri.equals("/home")
                || uri.equals("/courses") || uri.startsWith("/courses/")
                || uri.equals("/login") || uri.equals("/register")
                || uri.equals("/forgot-password") || uri.equals("/forgot-password/reset")
                || uri.equals("/about") || uri.equals("/contact");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = normalizedPath(request);
        String ctx = request.getContextPath() == null ? "" : request.getContextPath();
        HttpSession session = request.getSession();
        CsrfRequestDataValueProcessor.tokenFor(session);

        // ---- CSRF: mọi request thay đổi dữ liệu phải mang token của session ----
        String method = request.getMethod();
        boolean safeMethod = method.equals("GET") || method.equals("HEAD") || method.equals("OPTIONS") || method.equals("TRACE");
        if (!safeMethod && !csrfTokenValid(request, session)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token không hợp lệ hoặc đã hết hạn. Vui lòng tải lại trang.");
            return false;
        }

        if (isPublic(uri) || uri.equals("/logout")) {
            return true;
        }

        User sessionUser = (User) session.getAttribute(SESSION_USER);

        // Protected paths require login
        if (sessionUser == null) {
            if (request.getMethod().equals("GET")) {
                String redirectUrl = uri;
                if (request.getQueryString() != null) {
                    redirectUrl += "?" + request.getQueryString();
                }
                session.setAttribute("redirectAfterLogin", redirectUrl);
            }
            response.sendRedirect(ctx + "/login?message=require_login");
            return false;
        }

        // Luôn nạp lại user từ DB để khóa tài khoản / đổi quyền có hiệu lực ngay (kể cả admin/manager)
        Optional<User> freshOpt = userRepository.findById(sessionUser.getId());
        if (freshOpt.isEmpty()) {
            session.invalidate();
            response.sendRedirect(ctx + "/login?error=unauthorized");
            return false;
        }
        User currentUser = freshOpt.get();
        session.setAttribute(SESSION_USER, currentUser);

        // Account status check
        if (currentUser.getStatus() == UserStatus.BLOCKED) {
            session.invalidate();
            response.sendRedirect(ctx + "/login?error=blocked");
            return false;
        }

        // Role-based authorization
        if (uri.equals("/admin") || uri.startsWith("/admin/")) {
            if (currentUser.getRole() != Role.ADMIN) {
                response.sendRedirect(ctx + "/?error=access_denied");
                return false;
            }
        } else if (uri.equals("/manager") || uri.startsWith("/manager/")) {
            if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.MANAGER) {
                response.sendRedirect(ctx + "/?error=access_denied");
                return false;
            }
        }

        return true;
    }

    private boolean csrfTokenValid(HttpServletRequest request, HttpSession session) {
        Object expected = session.getAttribute(CsrfRequestDataValueProcessor.SESSION_TOKEN);
        String actual = request.getParameter(CsrfRequestDataValueProcessor.PARAM_NAME);
        if (!(expected instanceof String) || actual == null) return false;
        return MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null && modelAndView.getViewName() != null
                && !modelAndView.getViewName().startsWith("redirect:")) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User currentUser = (User) session.getAttribute(SESSION_USER);
                if (currentUser != null) {
                    modelAndView.addObject("currentUser", currentUser);
                }
            }
        }
    }
}
