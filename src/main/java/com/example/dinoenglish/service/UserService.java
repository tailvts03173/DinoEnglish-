package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Role;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.model.UserStatus;
import com.example.dinoenglish.repository.UserRepository;
import com.example.dinoenglish.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+ .-]{8,20}$");
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 72;

    /** Kiểm tra độ dài mật khẩu (không trim - khoảng trắng là ký tự hợp lệ). */
    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự!");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Mật khẩu tối đa " + MAX_PASSWORD_LENGTH + " ký tự!");
        }
    }

    @Transactional
    public User register(String username, String password, String fullName, String email, String phone) {
        // Chuẩn hóa TRƯỚC khi kiểm tra trùng
        String normUsername = username != null ? username.trim() : "";
        String normEmail = email != null ? email.trim().toLowerCase() : "";
        String normFullName = fullName != null ? fullName.trim() : "";
        String normPhone = phone != null ? phone.trim() : "";

        if (normUsername.isEmpty() || normEmail.isEmpty() || normFullName.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng điền đầy đủ tên đăng nhập, họ tên và email!");
        }
        if (!USERNAME.matcher(normUsername).matches()) {
            throw new IllegalArgumentException("Tên đăng nhập gồm 3-30 ký tự chữ, số, dấu chấm hoặc gạch dưới!");
        }
        if (normEmail.length() > 100 || !EMAIL.matcher(normEmail).matches()) {
            throw new IllegalArgumentException("Địa chỉ email không hợp lệ!");
        }
        if (normFullName.length() > 100) {
            throw new IllegalArgumentException("Họ tên quá dài!");
        }
        if (!normPhone.isEmpty() && !PHONE.matcher(normPhone).matches()) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ!");
        }
        validatePassword(password);

        if (userRepository.existsByUsernameIgnoreCase(normUsername)) {
            throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng!");
        }
        if (userRepository.existsByEmailIgnoreCase(normEmail)) {
            throw new IllegalArgumentException("Địa chỉ email đã được đăng ký!");
        }

        User user = User.builder()
                .username(normUsername)
                .password(PasswordUtil.hashPassword(password))
                .fullName(normFullName)
                .email(normEmail)
                .phone(normPhone.isEmpty() ? null : normPhone)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .totalXp(0)
                .currentLevel(1)
                .currentStreak(0)
                .maxStreak(0)
                .avatarUrl("/images/dino-logo.png")
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> authenticate(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || rawPassword == null) return Optional.empty();
        String login = usernameOrEmail.trim();
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(login, login.toLowerCase());
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        // Thử mật khẩu nguyên bản; thử thêm bản đã trim để tương thích tài khoản cũ (trước đây mật khẩu bị trim khi lưu)
        String matched = null;
        if (PasswordUtil.matches(rawPassword, user.getPassword())) {
            matched = rawPassword;
        } else if (!rawPassword.equals(rawPassword.trim())
                && PasswordUtil.matches(rawPassword.trim(), user.getPassword())) {
            matched = rawPassword.trim();
        }
        if (matched == null) return Optional.empty();

        if (PasswordUtil.needsRehash(user.getPassword())) {
            user.setPassword(PasswordUtil.hashPassword(matched));
            userRepository.save(user);
        }
        return Optional.of(user);
    }

    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl.trim());
        }

        return userRepository.save(user);
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (!PasswordUtil.matches(currentPassword, user.getPassword())) {
            return false;
        }

        validatePassword(newPassword);
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
        return true;
    }

    /** Đặt mật khẩu mới (dùng sau khi đã xác minh mã gửi qua email). */
    @Transactional
    public void setNewPassword(User user, String newPassword) {
        validatePassword(newPassword);
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User toggleUserStatus(Long userId, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        if (user.getId().equals(actorId)) {
            throw new IllegalArgumentException("Bạn không thể tự khóa tài khoản của chính mình!");
        }
        if (user.getStatus() == UserStatus.ACTIVE && isLastActiveAdmin(user)) {
            throw new IllegalArgumentException("Không thể khóa quản trị viên cuối cùng của hệ thống!");
        }
        user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.BLOCKED : UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional
    public User changeUserRole(Long userId, Role newRole, Long actorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));
        if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
            if (user.getId().equals(actorId)) {
                throw new IllegalArgumentException("Bạn không thể tự hạ quyền của chính mình!");
            }
            if (isLastActiveAdmin(user)) {
                throw new IllegalArgumentException("Không thể hạ quyền quản trị viên cuối cùng của hệ thống!");
            }
        }
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /** Dùng khi tạo tài khoản mới (không có "người thực hiện" nào bị ảnh hưởng). */
    @Transactional
    public User createUserWithRole(String username, String password, String fullName,
                                   String email, String phone, Role role) {
        User user = register(username, password, fullName, email, phone);
        if (role != Role.USER) {
            user.setRole(role);
            user = userRepository.save(user);
        }
        return user;
    }

    private boolean isLastActiveAdmin(User user) {
        return user.getRole() == Role.ADMIN
                && user.getStatus() == UserStatus.ACTIVE
                && userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE) <= 1;
    }

    public long countAllUsers() {
        return userRepository.count();
    }

    public long countUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }
}
