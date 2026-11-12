package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.CourseLevel;
import com.example.dinoenglish.model.Enrollment;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.repository.CourseRepository;
import com.example.dinoenglish.repository.EnrollmentRepository;
import com.example.dinoenglish.repository.LessonRepository;
import com.example.dinoenglish.repository.UserProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dinoenglish.util.UrlUtil;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository,
                         LessonRepository lessonRepository,
                         UserProgressRepository userProgressRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public List<Course> getAllActiveCourses() {
        return courseRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByLevel(CourseLevel level) {
        return courseRepository.findByLevelAndActiveTrueOrderByDisplayOrderAsc(level);
    }

    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    public Optional<Course> getCourseBySlug(String slug) {
        return courseRepository.findBySlug(slug);
    }

    @Transactional
    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * Lưu khóa học từ form: nếu đã có id thì nạp bản ghi cũ và chỉ chép các trường cho phép sửa,
     * giữ nguyên danh sách bài học / tài liệu (tránh orphanRemoval xóa sạch dữ liệu con).
     * Slug để trống khi sửa thì giữ slug cũ; khi tạo mới thì tự sinh và đảm bảo duy nhất.
     */
    @Transactional
    public Course saveCourseFromForm(Course form) {
        Course target;
        if (form.getId() != null) {
            target = courseRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khóa học!"));
        } else {
            target = new Course();
        }

        String slug = form.getSlug() != null ? form.getSlug().trim() : "";
        if (slug.isEmpty()) {
            slug = (target.getId() != null && target.getSlug() != null && !target.getSlug().isBlank())
                    ? target.getSlug()
                    : toSlug(form.getTitle());
        } else {
            slug = toSlug(slug);
        }
        if (slug.isEmpty()) slug = "khoa-hoc";
        String unique = slug;
        int n = 2;
        while (true) {
            Optional<Course> other = courseRepository.findBySlug(unique);
            if (other.isEmpty() || (target.getId() != null && other.get().getId().equals(target.getId()))) break;
            unique = slug + "-" + n++;
        }

        target.setTitle(form.getTitle());
        target.setSlug(unique);
        target.setDescription(form.getDescription());
        target.setImageUrl(UrlUtil.safeUrl(form.getImageUrl()));
        target.setLevel(form.getLevel());
        target.setDurationHours(form.getDurationHours());
        target.setDisplayOrder(form.getDisplayOrder());
        target.setActive(form.getActive() != null ? form.getActive() : Boolean.FALSE);
        return courseRepository.save(target);
    }

    /** Chuyển tiêu đề thành slug; xử lý đúng chữ "đ/Đ" và dấu tiếng Việt. */
    public static String toSlug(String input) {
        if (input == null) return "";
        String s = input.trim().replace('đ', 'd').replace('Đ', 'D');
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        s = s.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-+|-+$)", "");
        return s;
    }

    public long countActiveCourses() {
        return courseRepository.countByActiveTrue();
    }

    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }

    public boolean isUserEnrolled(Long userId, Long courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    public Optional<Enrollment> getEnrollment(Long userId, Long courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
    }

    public List<Enrollment> getUserEnrollments(Long userId) {
        return enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId);
    }

    @Transactional
    public Enrollment enrollUser(User user, Course course) {
        Optional<Enrollment> existing = enrollmentRepository.findByUserAndCourse(user, course);
        if (existing.isPresent()) {
            return existing.get();
        }

        Enrollment enrollment = Enrollment.builder()
                .user(user)
                .course(course)
                .progressPercent(0.0)
                .completed(false)
                .build();

        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void updateCourseProgress(Long userId, Long courseId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollmentOpt.isPresent()) {
            Enrollment enrollment = enrollmentOpt.get();
            long totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId);
            if (totalLessons == 0) {
                enrollment.setProgressPercent(0.0);
            } else {
                long completedLessons = userProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);
                double percent = (double) completedLessons / totalLessons * 100.0;
                enrollment.setProgressPercent(Math.min(100.0, Math.round(percent * 10.0) / 10.0));
                if (completedLessons >= totalLessons && !enrollment.getCompleted()) {
                    enrollment.setCompleted(true);
                    enrollment.setCompletedAt(LocalDateTime.now());
                }
            }
            enrollmentRepository.save(enrollment);
        }
    }

    public long countTotalCourses() {
        return courseRepository.count();
    }
}
