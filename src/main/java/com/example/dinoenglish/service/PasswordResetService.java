package com.example.dinoenglish.service;

import com.example.dinoenglish.model.PasswordResetToken;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserStatus;
import com.example.dinoenglish.repository.PasswordResetTokenRepository;
import com.example.dinoenglish.repository.UserRepository;
import com.example.dinoenglish.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Quên mật khẩu 2 bước: (1) nhập email -> gửi mã 6 số; (2) nhập mã + mật khẩu mới.
 * Mã hết hạn sau 10 phút, dùng 1 lần, tối đa 5 lần nhập sai, gửi lại cách nhau tối thiểu 60 giây.
 */
@Service
public class PasswordResetService {

    public static final int VALID_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    public enum Result { OK, INVALID_OR_EXPIRED, TOO_MANY_ATTEMPTS }

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final MailService mailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                UserService userService,
                                MailService mailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * Gửi mã nếu email thuộc một tài khoản đang hoạt động. Luôn không tiết lộ email có tồn tại hay không
     * (người gọi hiển thị cùng một thông báo cho mọi trường hợp).
     */
    @Transactional
    public void requestCode(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) return;
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(rawEmail.trim());
        if (userOpt.isEmpty()) return;
        User user = userOpt.get();
        if (user.getStatus() == UserStatus.BLOCKED) return;

        Optional<PasswordResetToken> last = tokenRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user);
        if (last.isPresent() && last.get().getCreatedAt().plus(RESEND_COOLDOWN).isAfter(LocalDateTime.now())) {
            return; // đã gửi gần đây, chống spam email
        }

        // Vô hiệu các mã cũ
        List<PasswordResetToken> old = tokenRepository.findByUserAndUsedFalse(user);
        for (PasswordResetToken t : old) t.setUsed(true);
        tokenRepository.saveAll(old);

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash(PasswordUtil.hashPassword(code));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(VALID_MINUTES));
        tokenRepository.save(token);

        mailService.sendResetCode(user.getEmail(), user.getFullName(), code, VALID_MINUTES);
    }

    /** Xác minh mã và đặt mật khẩu mới. Ném IllegalArgumentException nếu mật khẩu mới không hợp lệ. */
    @Transactional
    public Result resetPassword(String rawEmail, String code, String newPassword) {
        if (rawEmail == null || code == null) return Result.INVALID_OR_EXPIRED;
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(rawEmail.trim());
        if (userOpt.isEmpty()) return Result.INVALID_OR_EXPIRED;
        User user = userOpt.get();

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findFirstByUserAndUsedFalseOrderByCreatedAtDesc(user);
        if (tokenOpt.isEmpty()) return Result.INVALID_OR_EXPIRED;
        PasswordResetToken token = tokenOpt.get();

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            token.setUsed(true);
            tokenRepository.save(token);
            return Result.INVALID_OR_EXPIRED;
        }
        if (token.getAttempts() >= MAX_ATTEMPTS) {
            token.setUsed(true);
            tokenRepository.save(token);
            return Result.TOO_MANY_ATTEMPTS;
        }

        if (!PasswordUtil.matches(code.trim(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            if (token.getAttempts() >= MAX_ATTEMPTS) token.setUsed(true);
            tokenRepository.save(token);
            return token.getAttempts() >= MAX_ATTEMPTS ? Result.TOO_MANY_ATTEMPTS : Result.INVALID_OR_EXPIRED;
        }

        // Kiểm tra mật khẩu mới TRƯỚC khi đốt mã, để người dùng nhập lại được nếu mật khẩu quá ngắn
        UserService.validatePassword(newPassword);
        userService.setNewPassword(user, newPassword);
        token.setUsed(true);
        tokenRepository.save(token);
        return Result.OK;
    }
}
