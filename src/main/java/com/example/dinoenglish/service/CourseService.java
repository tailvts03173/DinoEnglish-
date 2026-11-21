package com.example.dinoenglish.service;

import com.example.dinoenglish.model.Course;
import com.example.dinoenglish.model.CourseLevel;
import com.example.dinoenglish.model.Enrollment;
import com.example.dinoenglish.model.Lesson;
import com.example.dinoenglish.model.User;
import com.example.dinoenglish.repository.CourseRepository;
import com.example.dinoenglish.repository.EnrollmentRepository;
import com.example.dinoenglish.repository.LessonRepository;
import com.example.dinoenglish.repository.QuizSubmissionRepository;
import com.example.dinoenglish.repository.UserProgressRepository;
import com.example.dinoenglish.util.UrlUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    public CourseService(CourseRepository courseRepository,
                         EnrollmentRepository enrollmentRepository,
                         LessonRepository lessonRepository,
                         UserProgressRepository userProgressRepository,
                         QuizSubmissionRepository quizSubmissionRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.userProgressRepository = userProgressRepository;
        this.quizSubmissionRepository = quizSubmissionRepository;
    }

    private static boolean hasId(String id) {
        return id != null && !id.isBlank();
    }

    /** MongoDB không có quan hệ: nạp danh sách bài học (bản nhẹ) vào Course.lessons (@Transient) để giao diện dùng như trước. */
    private void attachLessons(Collection<Course> courses) {
        for (Course c : courses) {
            List<Lesson> lessons = new ArrayList<>(lessonRepository.findLightByCourseId(c.getId()));
            lessons.sort(Comparator.comparing(Lesson::getOrderIndex, Comparator.nullsLast(Comparator.<Integer>naturalOrder())));
            c.setLessons(lessons);
        }
    }

    /** Nạp Enrollment.course (@Transient); bỏ qua đăng ký có khóa học đã bị xóa. */
    private void attachCourses(List<Enrollment> enrollments) {
        Set<String> ids = new HashSet<>();
        for (Enrollment e : enrollments) ids.add(e.getCourseId());
        Map<String, Course> byId = new HashMap<>();
        for (Course c : courseRepository.findAllById(ids)) byId.put(c.getId(), c);
        attachLessons(byId.values());
        for (Enrollment e : enrollments) e.setCourse(byId.get(e.getCourseId()));
        enrollments.removeIf(e -> e.getCourse() == null);
    }

    public List<Course> getAllActiveCourses() {
        List<Course> list = courseRepository.findByActiveTrueOrderByDisplayOrderAsc();
        attachLessons(list);
        return list;
    }

    public List<Course> getAllCourses() {
        List<Course> list = courseRepository.findAllByOrderByDisplayOrderAsc();
        attachLessons(list);
        return list;
    }

    public List<Course> getCoursesByLevel(CourseLevel level) {
        List<Course> list = courseRepository.findByLevelAndActiveTrueOrderByDisplayOrderAsc(level);
        attachLessons(list);
        return list;
    }

    public Optional<Course> getCourseById(String id) {
        if (!hasId(id)) return Optional.empty();
        Optional<Course> opt = courseRepository.findById(id);
        opt.ifPresent(c -> attachLessons(List.of(c)));
        return opt;
    }

    public Optional<Course> getCourseBySlug(String slug) {
        Optional<Course> opt = courseRepository.findBySlug(slug);
        opt.ifPresent(c -> attachLessons(List.of(c)));
        return opt;
    }

    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    /**
     * Lưu khóa học từ form: nếu đã có id thì nạp bản ghi cũ và chỉ chép các trường cho phép sửa,
     * giữ nguyên danh sách tài liệu (save() của MongoDB ghi đè cả document nên không được lưu thẳng object từ form).
     * Slug để trống khi sửa thì giữ slug cũ; khi tạo mới thì tự sinh và đảm bảo duy nhất.
     */
    public Course saveCourseFromForm(Course form) {
        Course target;
        if (hasId(form.getId())) {
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

    /**
     * MongoDB không có khóa ngoại nên phải tự kiểm tra: khóa học đã có học viên đăng ký / học thì không cho xóa
     * (ManagerController hiển thị gợi ý "hãy ẩn khóa học"). Nếu được xóa thì dọn luôn bài học và bài nộp liên quan.
     */
    public void deleteCourse(String id) {
        if (!hasId(id)) return;
        if (enrollmentRepository.countByCourseId(id) > 0 || userProgressRepository.existsByCourseId(id)) {
            throw new DataIntegrityViolationException("Khóa học đã có học viên đăng ký hoặc học");
        }
        for (Lesson l : lessonRepository.findLightByCourseId(id)) {
            quizSubmissionRepository.deleteByLessonId(l.getId());
        }
        lessonRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
    }

    public boolean isUserEnrolled(String userId, String courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    public Optional<Enrollment> getEnrollment(String userId, String courseId) {
        Optional<Enrollment> opt = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        opt.ifPresent(e -> courseRepository.findById(courseId).ifPresent(e::setCourse));
        return opt;
    }

    public List<Enrollment> getUserEnrollments(String userId) {
        List<Enrollment> list = new ArrayList<>(enrollmentRepository.findByUserIdOrderByEnrolledAtDesc(userId));
        attachCourses(list);
        return list;
    }

    public Enrollment enrollUser(User user, Course course) {
        Optional<Enrollment> existing = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId());
        if (existing.isPresent()) {
            existing.get().setCourse(course);
            return existing.get();
        }
        Enrollment enrollment = Enrollment.builder()
                .userId(user.getId())
                .courseId(course.getId())
                .progressPercent(0.0)
                .completed(false)
                .build();
        try {
            enrollment = enrollmentRepository.save(enrollment);
        } catch (DuplicateKeyException e) {
            // bấm đăng ký 2 lần liên tiếp: unique index (userId, courseId) chặn bản thứ hai
            enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId()).orElse(enrollment);
        }
        enrollment.setCourse(course);
        return enrollment;
    }

    public void updateCourseProgress(String userId, String courseId) {
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
        if (enrollmentOpt.isPresent()) {
            Enrollment enrollment = enrollmentOpt.get();
            long totalLessons = lessonRepository.countByCourseIdAndActiveTrue(courseId);
            if (totalLessons == 0) {
                enrollment.setProgressPercent(0.0);
            } else {
                long completedLessons = userProgressRepository.countByUserIdAndCourseIdAndCompletedTrue(userId, courseId);
                double percent = (double) completedLessons / totalLessons * 100.0;
                enrollment.setProgressPercent(Math.min(100.0, Math.round(percent * 10.0) / 10.0));
                if (completedLessons >= totalLessons && !Boolean.TRUE.equals(enrollment.getCompleted())) {
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
