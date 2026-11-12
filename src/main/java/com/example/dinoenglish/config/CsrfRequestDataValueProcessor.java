package com.example.dinoenglish.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.RequestDataValueProcessor;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Tự động chèn input ẩn "_csrf" vào mọi form (Thymeleaf gọi processor này khi render th:action),
 * nên không cần sửa từng template. AuthInterceptor kiểm tra token này cho mọi request POST.
 * Bean phải có tên "requestDataValueProcessor".
 */
@Component("requestDataValueProcessor")
public class CsrfRequestDataValueProcessor implements RequestDataValueProcessor {

    public static final String PARAM_NAME = "_csrf";
    public static final String SESSION_TOKEN = "CSRF_TOKEN";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String tokenFor(HttpSession session) {
        Object existing = session.getAttribute(SESSION_TOKEN);
        if (existing instanceof String) return (String) existing;
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_TOKEN, token);
        return token;
    }

    @Override
    public String processAction(HttpServletRequest request, String action, String httpMethod) {
        return action;
    }

    @Override
    public String processFormFieldValue(HttpServletRequest request, String name, String value, String type) {
        return value;
    }

    @Override
    public Map<String, String> getExtraHiddenFields(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        fields.put(PARAM_NAME, tokenFor(request.getSession()));
        return fields;
    }

    @Override
    public String processUrl(HttpServletRequest request, String url) {
        return url;
    }
}
