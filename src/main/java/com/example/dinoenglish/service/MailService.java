package com.example.dinoenglish.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Gửi email qua Gmail SMTP (cấu hình trong application.properties / application-local.properties). */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(JavaMailSender mailSender,
                       @Value("${app.mail.from:${spring.mail.username:}}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** @return true nếu gửi thành công; false nếu lỗi (đã ghi log, không ném ngoại lệ ra giao diện). */
    public boolean sendResetCode(String to, String fullName, String code, int validMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        if (from != null && !from.isBlank()) msg.setFrom("Dino English <" + from + ">");
        msg.setTo(to);
        msg.setSubject("[Dino English] Mã đặt lại mật khẩu của bạn");
        msg.setText("Xin chào " + (fullName != null ? fullName : "bạn") + ",\n\n"
                + "Mã xác minh để đặt lại mật khẩu Dino English của bạn là:\n\n"
                + "        " + code + "\n\n"
                + "Mã có hiệu lực trong " + validMinutes + " phút và chỉ dùng được một lần.\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này - mật khẩu của bạn vẫn an toàn.\n"
                + "Tuyệt đối không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Dino English");
        try {
            mailSender.send(msg);
            return true;
        } catch (Exception e) {
            log.error("Không gửi được email đặt lại mật khẩu tới {}: {} "
                    + "(kiểm tra spring.mail.username / spring.mail.password - Gmail yêu cầu Mật khẩu ứng dụng 16 ký tự)",
                    to, e.getMessage());
            return false;
        }
    }
}
