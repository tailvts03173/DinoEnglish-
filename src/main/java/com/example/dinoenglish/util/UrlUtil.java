package com.example.dinoenglish.util;

import java.net.URI;

/** Chỉ cho phép URL http(s) hoặc đường dẫn nội bộ; chặn javascript:, data: ... (chống XSS lưu trữ). */
public final class UrlUtil {

    private UrlUtil() {
    }

    public static String safeUrl(String url) {
        if (url == null) return null;
        String trimmed = url.trim();
        if (trimmed.isEmpty()) return trimmed;
        // Đường dẫn nội bộ (/images/...), nhưng không phải "//host" (protocol-relative)
        if (trimmed.startsWith("/") && !trimmed.startsWith("//")) return trimmed;
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && uri.getHost() != null) {
                return trimmed;
            }
        } catch (Exception ignored) {
            // rơi xuống trả về lỗi bên dưới
        }
        throw new IllegalArgumentException("URL không hợp lệ (chỉ chấp nhận http:// hoặc https://): " + trimmed);
    }
}
