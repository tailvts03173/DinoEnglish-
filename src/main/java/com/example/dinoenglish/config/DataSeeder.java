package com.example.dinoenglish.config;

import com.example.dinoenglish.model.*;
import com.example.dinoenglish.repository.*;
import com.example.dinoenglish.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final BadgeRepository badgeRepository;
    private final DocumentResourceRepository documentResourceRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Value("${app.seed.admin-email:admin@dinoenglish.edu.vn}")
    private String adminEmail;
    @Value("${app.seed.admin-password:}")
    private String adminPassword;
    @Value("${app.seed.manager-password:}")
    private String managerPassword;

    private String passwordOrRandom(String configured, String label) {
        if (configured != null && !configured.isBlank()) return configured;
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        log.warn("Mật khẩu {} được sinh ngẫu nhiên (chỉ hiện một lần, hãy đổi sau khi đăng nhập): {}", label, sb);
        return sb.toString();
    }

    public DataSeeder(UserRepository userRepository,
                      CourseRepository courseRepository,
                      LessonRepository lessonRepository,
                      VocabularyRepository vocabularyRepository,
                      QuizQuestionRepository quizQuestionRepository,
                      BadgeRepository badgeRepository,
                      DocumentResourceRepository documentResourceRepository,
                      EnrollmentRepository enrollmentRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.badgeRepository = badgeRepository;
        this.documentResourceRepository = documentResourceRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        seedBadges();
        seedUsers();
        seedCoursesAndLessons();
        seedDocuments();
    }

    private void seedBadges() {
        if (badgeRepository.count() > 0) return;

        List<Badge> badges = List.of(
                Badge.builder().code("FIRST_STEP").title("Bước Chân Đầu Tiên")
                        .description("Hoàn thành bài học tiếng Anh đầu tiên trên Dino English")
                        .iconClass("fa-paw text-success").xpBonus(50).build(),
                Badge.builder().code("STREAK_7").title("Chiến Binh 7 Ngày")
                        .description("Duy trì chuỗi học liên tục suốt 7 ngày")
                        .iconClass("fa-fire text-danger").xpBonus(100).build(),
                Badge.builder().code("STREAK_14").title("Bậc Thầy Kiên Trì")
                        .description("Duy trì chuỗi học liên tục suốt 14 ngày")
                        .iconClass("fa-bolt text-warning").xpBonus(200).build(),
                Badge.builder().code("STREAK_30").title("Huyền Thoại Khủng Long")
                        .description("Chinh phục chuỗi ngày học 30 ngày kỳ diệu")
                        .iconClass("fa-crown text-warning").xpBonus(500).build(),
                Badge.builder().code("QUIZ_ACE").title("Thánh Điểm 10")
                        .description("Đạt điểm số tuyệt đối 100% trong bài kiểm tra trắc nghiệm")
                        .iconClass("fa-medal text-primary").xpBonus(80).build(),
                Badge.builder().code("XP_500").title("Nhà Khám Phá Tri Thức")
                        .description("Tích lũy đạt mốc 500 điểm kinh nghiệm XP")
                        .iconClass("fa-gem text-info").xpBonus(100).build(),
                Badge.builder().code("XP_1000").title("Đại Sư Dino")
                        .description("Chinh phục cột mốc đỉnh cao 1000 điểm XP")
                        .iconClass("fa-award text-purple").xpBonus(250).build()
        );
        badgeRepository.saveAll(badges);
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        // Admin Account
        User admin = User.builder()
                .username("admin")
                .password(PasswordUtil.hashPassword(passwordOrRandom(adminPassword, "admin")))
                .fullName("Quản Trị Viên Dino")
                .email(adminEmail.trim().toLowerCase())
                .phone("0987654321")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .avatarUrl("/images/dino-logo.png")
                .totalXp(1200)
                .currentLevel(5)
                .currentStreak(15)
                .maxStreak(15)
                .lastStudyDate(com.example.dinoenglish.service.GamificationService.today())
                .build();

        // Manager Account
        User manager = User.builder()
                .username("manager")
                .password(PasswordUtil.hashPassword(passwordOrRandom(managerPassword, "manager")))
                .fullName("Thầy Giáo Dino (Quản Lý)")
                .email("manager@dinoenglish.edu.vn")
                .phone("0912345678")
                .role(Role.MANAGER)
                .status(UserStatus.ACTIVE)
                .avatarUrl("/images/dino-logo.png")
                .totalXp(650)
                .currentLevel(3)
                .currentStreak(8)
                .maxStreak(10)
                .lastStudyDate(com.example.dinoenglish.service.GamificationService.today())
                .build();

        // Student Accounts
        User student1 = User.builder()
                .username("dino_learner")
                .password(PasswordUtil.hashPassword("123456"))
                .fullName("Nguyễn Văn Học")
                .email("student@dinoenglish.edu.vn")
                .phone("0934567890")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .avatarUrl("/images/dino-logo.png")
                .totalXp(420)
                .currentLevel(3)
                .currentStreak(5)
                .maxStreak(7)
                .lastStudyDate(com.example.dinoenglish.service.GamificationService.today())
                .build();

        User student2 = User.builder()
                .username("hieu_hoc")
                .password(PasswordUtil.hashPassword("123456"))
                .fullName("Trần Thị Chăm Chỉ")
                .email("chamchi@dinoenglish.edu.vn")
                .phone("0945678901")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .avatarUrl("/images/dino-logo.png")
                .totalXp(890)
                .currentLevel(4)
                .currentStreak(14)
                .maxStreak(14)
                .lastStudyDate(com.example.dinoenglish.service.GamificationService.today())
                .build();

        User student3 = User.builder()
                .username("anh_sao")
                .password(PasswordUtil.hashPassword("123456"))
                .fullName("Lê Ánh Sao")
                .email("anhsao@dinoenglish.edu.vn")
                .phone("0956789012")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .avatarUrl("/images/dino-logo.png")
                .totalXp(180)
                .currentLevel(2)
                .currentStreak(3)
                .maxStreak(3)
                .lastStudyDate(com.example.dinoenglish.service.GamificationService.today().minusDays(1))
                .build();

        userRepository.saveAll(List.of(admin, manager, student1, student2, student3));
    }

    private void seedCoursesAndLessons() {
        if (courseRepository.count() > 0) return;

        // 1. Beginner Course A1
        Course c1 = Course.builder()
                .title("Khám Phá Tiếng Anh Cơ Bản (A1)")
                .slug("kham-pha-tieng-anh-co-ban-a1")
                .description("Khóa học nền tảng toàn diện dành cho người mới bắt đầu. Làm quen với bảng chữ cái, phát âm chuẩn, từ vựng thông dụng và các cấu trúc câu giao tiếp căn bản nhất.")
                .imageUrl("https://images.unsplash.com/photo-1546410531-bb4caa6b424d?auto=format&fit=crop&w=600&q=80")
                .level(CourseLevel.BEGINNER_A1)
                .durationHours(24)
                .displayOrder(1)
                .active(true)
                .build();
        c1 = courseRepository.save(c1);

        // Lesson 1: Animals Around Us (Vocabulary)
        Lesson l1 = Lesson.builder()
                .course(c1)
                .title("Bài 1: Thế Giới Động Vật Thân Quen (Animals Around Us)")
                .description("Học 8 từ vựng tiếng Anh phổ biến về các loài động vật kèm phiên âm, hình ảnh sống động và phát âm chuẩn.")
                .lessonType(LessonType.VOCABULARY)
                .orderIndex(1)
                .xpReward(30)
                .content("Chào mừng bạn đến với bài học đầu tiên! Động vật là một trong những chủ đề thú vị và gần gũi nhất. Hãy bấm vào biểu tượng chiếc loa bên cạnh từng từ vựng để nghe Dino phát âm chuẩn nhé!")
                .active(true)
                .build();
        l1 = lessonRepository.save(l1);

        List<Vocabulary> vocabList = List.of(
                Vocabulary.builder().lesson(l1).word("Elephant").phonetic("/ˈel.ɪ.fənt/").partOfSpeech("noun")
                        .meaningVi("Con voi").exampleEn("The elephant is the largest land animal.")
                        .exampleVi("Voi là loài động vật trên cạn lớn nhất.")
                        .imageUrl("https://images.unsplash.com/photo-1557050543-4d5f4e07ef46?auto=format&fit=crop&w=400&q=80").build(),
                Vocabulary.builder().lesson(l1).word("Dolphin").phonetic("/ˈdɒl.fɪn/").partOfSpeech("noun")
                        .meaningVi("Cá heo").exampleEn("Dolphins are very friendly and intelligent.")
                        .exampleVi("Cá heo rất thân thiện và thông minh.")
                        .imageUrl("https://images.unsplash.com/photo-1570481662006-a3a1374699e8?auto=format&fit=crop&w=400&q=80").build(),
                Vocabulary.builder().lesson(l1).word("Tiger").phonetic("/ˈtaɪ.ɡər/").partOfSpeech("noun")
                        .meaningVi("Con hổ").exampleEn("The Bengal tiger has beautiful orange and black stripes.")
                        .exampleVi("Hổ Bengal có những sọc màu cam và đen rất đẹp.")
                        .imageUrl("https://images.unsplash.com/photo-1549480017-d76466a4b7e8?auto=format&fit=crop&w=400&q=80").build(),
                Vocabulary.builder().lesson(l1).word("Rabbit").phonetic("/ˈræb.ɪt/").partOfSpeech("noun")
                        .meaningVi("Con thỏ").exampleEn("The white rabbit loves eating carrots.")
                        .exampleVi("Chú thỏ trắng rất thích ăn cà rốt.")
                        .imageUrl("https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=400&q=80").build(),
                Vocabulary.builder().lesson(l1).word("Penguin").phonetic("/ˈpeŋ.ɡwɪn/").partOfSpeech("noun")
                        .meaningVi("Chim cánh cụt").exampleEn("Penguins cannot fly, but they are great swimmers.")
                        .exampleVi("Chim cánh cụt không biết bay nhưng bơi rất cừ.")
                        .imageUrl("https://images.unsplash.com/photo-1598439210625-5067c578f3f6?auto=format&fit=crop&w=400&q=80").build(),
                Vocabulary.builder().lesson(l1).word("Lion").phonetic("/ˈlaɪ.ən/").partOfSpeech("noun")
                        .meaningVi("Sư tử").exampleEn("The lion is known as the king of the jungle.")
                        .exampleVi("Sư tử được biết đến là chúa tể rừng xanh.")
                        .imageUrl("https://images.unsplash.com/photo-1614027164847-1b28cfe1df60?auto=format&fit=crop&w=400&q=80").build()
        );
        vocabularyRepository.saveAll(vocabList);

        // Lesson 2: Subject Pronouns & Verb To Be (Grammar)
        Lesson l2 = Lesson.builder()
                .course(c1)
                .title("Bài 2: Đại Từ Nhân Xưng & Động Từ To Be (Subject Pronouns & Verb To Be)")
                .description("Nắm vững cách sử dụng I, You, He, She, It, We, They và các dạng tương ứng của To Be (am, is, are).")
                .lessonType(LessonType.GRAMMAR)
                .orderIndex(2)
                .xpReward(25)
                .content("### 1. ĐẠI TỪ NHÂN XƯNG (Subject Pronouns)\n" +
                        "Đại từ nhân xưng đứng ở vị trí chủ ngữ trong câu:\n" +
                        "- **I**: Tôi (ngôi thứ nhất số ít)\n" +
                        "- **You**: Bạn, các bạn\n" +
                        "- **He / She / It**: Anh ấy / Cô ấy / Nó (ngôi thứ ba số ít)\n" +
                        "- **We**: Chúng tôi, chúng ta\n" +
                        "- **They**: Họ, chúng nó\n\n" +
                        "### 2. ĐỘNG TỪ TO BE Ở HIỆN TẠI ĐƠN\n" +
                        "Động từ To Be có 3 dạng: **AM**, **IS**, **ARE**.\n" +
                        "- I + **am** *(Ví dụ: I am a student - Tôi là học sinh)*\n" +
                        "- He / She / It + **is** *(Ví dụ: Dino is cute - Dino rất dễ thương)*\n" +
                        "- You / We / They + **are** *(Ví dụ: We are ready! - Chúng tôi đã sẵn sàng!)*\n\n" +
                        "### 3. THỂ PHỦ ĐỊNH & NGHI VẤN\n" +
                        "- Phủ định: Thêm **not** sau To Be (am not, is not / isn't, are not / aren't).\n" +
                        "- Nghi vấn: Đảo To Be lên đầu câu (Are you happy? - Yes, I am!)")
                .active(true)
                .build();
        lessonRepository.save(l2);

        // Lesson 3: Quiz Animals & Pronouns
        Lesson l3 = Lesson.builder()
                .course(c1)
                .title("Bài 3: Trắc Nghiệm Kiểm Tra: Từ Vựng Động Vật & To Be")
                .description("Thử thách trắc nghiệm 5 câu hỏi nhanh để củng cố kiến thức và nhận ngay 50 XP cùng điểm thưởng Streak!")
                .lessonType(LessonType.QUIZ)
                .orderIndex(3)
                .xpReward(50)
                .content("Hãy đọc kỹ câu hỏi và chọn đáp án chính xác nhất (A, B, C hoặc D). Bạn cần đạt từ 60% điểm trở lên để vượt qua bài học!")
                .active(true)
                .build();
        l3 = lessonRepository.save(l3);

        List<QuizQuestion> quizQuestions = List.of(
                QuizQuestion.builder().lesson(l3)
                        .question("Từ nào sau đây trong tiếng Anh có nghĩa là 'Con voi'?")
                        .optionA("Dolphin").optionB("Elephant").optionC("Penguin").optionD("Tiger")
                        .correctOption("B")
                        .explanation("Elephant (/ˈel.ɪ.fənt/) có nghĩa là con voi.")
                        .orderIndex(1).build(),
                QuizQuestion.builder().lesson(l3)
                        .question("Điền động từ To Be thích hợp vào chỗ trống: 'She ___ my English teacher.'")
                        .optionA("am").optionB("are").optionC("is").optionD("be")
                        .correctOption("C")
                        .explanation("Chủ ngữ là ngôi thứ 3 số ít 'She', do đó động từ To Be đi kèm là 'is'.")
                        .orderIndex(2).build(),
                QuizQuestion.builder().lesson(l3)
                        .question("Loài chim nào không biết bay nhưng bơi lội rất giỏi ở vùng lạnh?")
                        .optionA("Lion").optionB("Rabbit").optionC("Penguin").optionD("Elephant")
                        .correctOption("C")
                        .explanation("Penguin (chim cánh cụt) sống ở vùng băng tuyết, không biết bay nhưng bơi rất nhanh.")
                        .orderIndex(3).build(),
                QuizQuestion.builder().lesson(l3)
                        .question("Chọn câu phủ định đúng với chủ ngữ 'They':")
                        .optionA("They isn't students.").optionB("They aren't students.")
                        .optionC("They am not students.").optionD("They not are students.")
                        .correctOption("B")
                        .explanation("Chủ ngữ số nhiều 'They' đi với 'are not', viết tắt là 'aren't'.")
                        .orderIndex(4).build(),
                QuizQuestion.builder().lesson(l3)
                        .question("Từ 'Tiger' trong tiếng Anh có phiên âm chuẩn là:")
                        .optionA("/ˈtaɪ.ɡər/").optionB("/ˈtɪɡ.ər/").optionC("/ˈteɪ.ɡər/").optionD("/ˈtuː.ɡər/")
                        .correctOption("A")
                        .explanation("Tiger phát âm là /ˈtaɪ.ɡər/.")
                        .orderIndex(5).build()
        );
        quizQuestionRepository.saveAll(quizQuestions);

        // 2. Elementary Course A2
        Course c2 = Course.builder()
                .title("Giao Tiếp Hàng Ngày & Ngữ Pháp Nền Tảng (A2)")
                .slug("giao-tiep-hang-ngay-ngu-phap-a2")
                .description("Phát triển khả năng phản xạ và giao tiếp trong các tình huống thực tế: mua sắm, gọi món, hỏi đường và thì Quá khứ đơn.")
                .imageUrl("https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=600&q=80")
                .level(CourseLevel.ELEMENTARY_A2)
                .durationHours(36)
                .displayOrder(2)
                .active(true)
                .build();
        c2 = courseRepository.save(c2);

        Lesson l2_1 = Lesson.builder()
                .course(c2)
                .title("Bài 1: Thói Quen Hàng Ngày (Daily Routines)")
                .description("Từ vựng về sinh hoạt hàng ngày, giờ giấc và thói quen tích cực.")
                .lessonType(LessonType.VOCABULARY)
                .orderIndex(1)
                .xpReward(30)
                .content("Học cách miêu tả một ngày của bạn bằng tiếng Anh tự nhiên nhất!")
                .active(true)
                .build();
        lessonRepository.save(l2_1);

        // 3. Intermediate Course B1
        Course c3 = Course.builder()
                .title("Tiếng Anh Đọc Hiểu & Giao Tiếp Tự Tin (B1)")
                .slug("tieng-anh-doc-hieu-giao-tiep-b1")
                .description("Nâng tầm vốn từ vựng học thuật, rèn luyện kỹ năng đọc hiểu bài viết dài, tranh luận và viết thư giao dịch cơ bản.")
                .imageUrl("https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=600&q=80")
                .level(CourseLevel.INTERMEDIATE_B1)
                .durationHours(48)
                .displayOrder(3)
                .active(true)
                .build();
        c3 = courseRepository.save(c3);

        // 4. Advanced Course B2
        Course c4 = Course.builder()
                .title("Từ Vựng Nâng Cao & Thuyết Trình Chuyên Sâu (B2)")
                .slug("tu-vung-nang-cao-thuyet-trinh-b2")
                .description("Khóa học chuẩn bị cho các chứng chỉ quốc tế và môi trường làm việc chuyên nghiệp đa quốc gia.")
                .imageUrl("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80")
                .level(CourseLevel.ADVANCED_B2)
                .durationHours(60)
                .displayOrder(4)
                .active(true)
                .build();
        c4 = courseRepository.save(c4);

        // Auto enroll student1 in course 1
        User student1 = userRepository.findByUsername("dino_learner").orElse(null);
        if (student1 != null) {
            Enrollment e = Enrollment.builder()
                    .user(student1)
                    .course(c1)
                    .progressPercent(33.3)
                    .completed(false)
                    .build();
            enrollmentRepository.save(e);
        }
    }

    private void seedDocuments() {
        if (documentResourceRepository.count() > 0) return;

        Course c1 = courseRepository.findBySlug("kham-pha-tieng-anh-co-ban-a1").orElse(null);

        List<DocumentResource> docs = List.of(
                DocumentResource.builder()
                        .course(c1)
                        .title("Cẩm Nang 360 Động Từ Bất Quy Tắc Thông Dụng Nhất")
                        .description("Tài liệu tổng hợp bảng động từ bất quy tắc tiếng Anh kèm phiên âm và nghĩa tiếng Việt chi tiết.")
                        .fileType("PDF")
                        .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                        .fileSize("2.4 MB")
                        .build(),
                DocumentResource.builder()
                        .course(c1)
                        .title("Sổ Tay 1000 Từ Vựng A1 - A2 Kèm Hình Ảnh")
                        .description("Toàn bộ từ vựng cơ bản theo 20 chủ đề thông dụng nhất có hình minh họa sinh động.")
                        .fileType("PDF")
                        .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                        .fileSize("5.8 MB")
                        .build(),
                DocumentResource.builder()
                        .course(c1)
                        .title("Bộ File Nghe Giao Tiếp Tiếng Anh Hàng Ngày (MP3)")
                        .description("Tuyển tập 30 đoạn hội thoại tiếng Anh bản xứ giọng US/UK chuẩn.")
                        .fileType("AUDIO")
                        .fileUrl("https://actions.google.com/sounds/v1/water/rain_heavy.ogg")
                        .fileSize("14.2 MB")
                        .build(),
                DocumentResource.builder()
                        .course(c1)
                        .title("Tuyển Tập 100 Bài Tập Trắc Nghiệm Ngữ Pháp Cơ Bản")
                        .description("Đề luyện tập có kèm lời giải và hướng dẫn từng bước của Dino English.")
                        .fileType("EXERCISE")
                        .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                        .fileSize("1.8 MB")
                        .build()
        );
        documentResourceRepository.saveAll(docs);
    }
}
