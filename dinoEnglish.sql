use DinoEnglishDB


/* ---------- Xóa bảng cũ (theo thứ tự phụ thuộc) ---------- */
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_badges;
DROP TABLE IF EXISTS quiz_submissions;
DROP TABLE IF EXISTS user_progress;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS quiz_questions;
DROP TABLE IF EXISTS vocabularies;
DROP TABLE IF EXISTS document_resources;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS badges;
DROP TABLE IF EXISTS users;
GO
 
/* ---------- users ---------- */
CREATE TABLE users (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    username        NVARCHAR(50)  NOT NULL,
    password        NVARCHAR(100) NOT NULL,
    full_name       NVARCHAR(100) NOT NULL,
    email           NVARCHAR(100) NOT NULL,
    phone           NVARCHAR(20)  NULL,
    avatar_url      NVARCHAR(255) NULL CONSTRAINT df_users_avatar DEFAULT N'/images/dino-logo.png',
    role            VARCHAR(20)   NOT NULL CONSTRAINT df_users_role DEFAULT 'USER',
    status          VARCHAR(20)   NOT NULL CONSTRAINT df_users_status DEFAULT 'ACTIVE',
    total_xp        INT           NOT NULL CONSTRAINT df_users_xp DEFAULT 0,
    current_level   INT           NOT NULL CONSTRAINT df_users_level DEFAULT 1,
    current_streak  INT           NOT NULL CONSTRAINT df_users_streak DEFAULT 0,
    max_streak      INT           NOT NULL CONSTRAINT df_users_maxstreak DEFAULT 0,
    last_study_date DATE          NULL,
    created_at      DATETIME2(6)  NOT NULL CONSTRAINT df_users_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT ck_users_role     CHECK (role IN ('ADMIN','MANAGER','USER')),
    CONSTRAINT ck_users_status   CHECK (status IN ('ACTIVE','BLOCKED'))
);
GO
 
/* ---------- badges ---------- */
CREATE TABLE badges (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    code        NVARCHAR(50)  NOT NULL,
    title       NVARCHAR(100) NOT NULL,
    description NVARCHAR(255) NULL,
    icon_class  NVARCHAR(100) NOT NULL,
    xp_bonus    INT           NOT NULL CONSTRAINT df_badges_xp DEFAULT 50,
    CONSTRAINT uq_badges_code UNIQUE (code)
);
GO
 
/* ---------- courses ---------- */
CREATE TABLE courses (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    title          NVARCHAR(150)  NOT NULL,
    slug           NVARCHAR(100)  NOT NULL,
    description    NVARCHAR(2000) NULL,
    image_url      NVARCHAR(255)  NULL,
    level          VARCHAR(30)    NOT NULL,
    duration_hours INT            NOT NULL CONSTRAINT df_courses_duration DEFAULT 20,
    display_order  INT            NOT NULL CONSTRAINT df_courses_order DEFAULT 1,
    active         BIT            NOT NULL CONSTRAINT df_courses_active DEFAULT 1,
    created_at     DATETIME2(6)   NOT NULL CONSTRAINT df_courses_created DEFAULT SYSDATETIME(),
    CONSTRAINT uq_courses_slug  UNIQUE (slug),
    CONSTRAINT ck_courses_level CHECK (level IN ('BEGINNER_A1','ELEMENTARY_A2','INTERMEDIATE_B1','ADVANCED_B2'))
);
GO
 
/* ---------- lessons ---------- */
CREATE TABLE lessons (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id   BIGINT        NOT NULL,
    title       NVARCHAR(150) NOT NULL,
    description NVARCHAR(500) NULL,
    lesson_type VARCHAR(30)   NOT NULL CONSTRAINT df_lessons_type DEFAULT 'VOCABULARY',
    order_index INT           NOT NULL CONSTRAINT df_lessons_order DEFAULT 1,
    xp_reward   INT           NOT NULL CONSTRAINT df_lessons_xp DEFAULT 20,
    content     NVARCHAR(MAX) NULL,
    audio_url   NVARCHAR(255) NULL,
    video_url   NVARCHAR(255) NULL,
    active      BIT           NOT NULL CONSTRAINT df_lessons_active DEFAULT 1,
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT ck_lessons_type CHECK (lesson_type IN
        ('VOCABULARY','GRAMMAR','LISTENING','READING','QUIZ','COMPREHENSIVE'))
);
CREATE INDEX ix_lessons_course_order ON lessons(course_id, order_index);
GO
 
/* ---------- document_resources ---------- */
CREATE TABLE document_resources (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    course_id   BIGINT        NULL,
    title       NVARCHAR(150) NOT NULL,
    description NVARCHAR(500) NULL,
    file_type   VARCHAR(20)   NOT NULL CONSTRAINT df_docs_type DEFAULT 'PDF',  -- PDF, AUDIO, EXERCISE, DOC
    file_url    NVARCHAR(255) NOT NULL,
    file_size   NVARCHAR(50)  NULL,
    uploaded_at DATETIME2(6)  NOT NULL CONSTRAINT df_docs_uploaded DEFAULT SYSDATETIME(),
    CONSTRAINT fk_docs_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);
CREATE INDEX ix_docs_course ON document_resources(course_id);
GO
 
/* ---------- vocabularies ---------- */
CREATE TABLE vocabularies (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    lesson_id      BIGINT        NOT NULL,
    word           NVARCHAR(100) NOT NULL,
    phonetic       NVARCHAR(100) NULL,
    part_of_speech NVARCHAR(50)  NULL,
    meaning_vi     NVARCHAR(255) NOT NULL,
    example_en     NVARCHAR(500) NULL,
    example_vi     NVARCHAR(500) NULL,
    image_url      NVARCHAR(255) NULL,
    audio_url      NVARCHAR(255) NULL,
    CONSTRAINT fk_vocab_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE
);
CREATE INDEX ix_vocab_lesson ON vocabularies(lesson_id);
GO
 
/* ---------- quiz_questions ---------- */
CREATE TABLE quiz_questions (
    id             BIGINT IDENTITY(1,1) PRIMARY KEY,
    lesson_id      BIGINT         NOT NULL,
    question       NVARCHAR(1000) NOT NULL,
    option_a       NVARCHAR(500)  NOT NULL,
    option_b       NVARCHAR(500)  NOT NULL,
    option_c       NVARCHAR(500)  NOT NULL,
    option_d       NVARCHAR(500)  NOT NULL,
    correct_option VARCHAR(5)     NOT NULL,
    explanation    NVARCHAR(1000) NULL,
    order_index    INT            NOT NULL CONSTRAINT df_quizq_order DEFAULT 1,
    CONSTRAINT fk_quizq_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT ck_quizq_correct CHECK (correct_option IN ('A','B','C','D'))
);
CREATE INDEX ix_quizq_lesson_order ON quiz_questions(lesson_id, order_index);
GO
 
/* ---------- enrollments ---------- */
CREATE TABLE enrollments (
    id               BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    course_id        BIGINT       NOT NULL,
    progress_percent FLOAT        NOT NULL CONSTRAINT df_enr_progress DEFAULT 0,
    completed        BIT          NOT NULL CONSTRAINT df_enr_completed DEFAULT 0,
    enrolled_at      DATETIME2(6) NOT NULL CONSTRAINT df_enr_enrolled DEFAULT SYSDATETIME(),
    completed_at     DATETIME2(6) NULL,
    CONSTRAINT fk_enr_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT uq_enr_user_course UNIQUE (user_id, course_id),
    CONSTRAINT ck_enr_progress CHECK (progress_percent BETWEEN 0 AND 100)
);
CREATE INDEX ix_enr_course ON enrollments(course_id);
GO
 
/* ---------- user_progress ---------- */
CREATE TABLE user_progress (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    lesson_id    BIGINT       NOT NULL,
    completed    BIT          NOT NULL CONSTRAINT df_up_completed DEFAULT 1,
    completed_at DATETIME2(6) NOT NULL CONSTRAINT df_up_completed_at DEFAULT SYSDATETIME(),
    CONSTRAINT fk_up_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_up_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    CONSTRAINT uq_up_user_lesson UNIQUE (user_id, lesson_id)
);
CREATE INDEX ix_up_lesson ON user_progress(lesson_id);
GO
 
/* ---------- quiz_submissions ---------- */
CREATE TABLE quiz_submissions (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    lesson_id       BIGINT       NOT NULL,
    score           INT          NOT NULL,   -- phần trăm 0-100
    total_questions INT          NOT NULL,
    correct_count   INT          NOT NULL,
    passed          BIT          NOT NULL CONSTRAINT df_qs_passed DEFAULT 0,
    xp_earned       INT          NOT NULL CONSTRAINT df_qs_xp DEFAULT 0,
    submitted_at    DATETIME2(6) NOT NULL CONSTRAINT df_qs_submitted DEFAULT SYSDATETIME(),
    CONSTRAINT fk_qs_user   FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE,
    CONSTRAINT fk_qs_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id),
    CONSTRAINT ck_qs_score  CHECK (score BETWEEN 0 AND 100)
);
CREATE INDEX ix_qs_user_lesson ON quiz_submissions(user_id, lesson_id);
CREATE INDEX ix_qs_lesson ON quiz_submissions(lesson_id);
GO
 
/* ---------- user_badges ---------- */
CREATE TABLE user_badges (
    id        BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id   BIGINT       NOT NULL,
    badge_id  BIGINT       NOT NULL,
    earned_at DATETIME2(6) NOT NULL CONSTRAINT df_ub_earned DEFAULT SYSDATETIME(),
    CONSTRAINT fk_ub_user  FOREIGN KEY (user_id)  REFERENCES users(id)  ON DELETE CASCADE,
    CONSTRAINT fk_ub_badge FOREIGN KEY (badge_id) REFERENCES badges(id),
    CONSTRAINT uq_ub_user_badge UNIQUE (user_id, badge_id)
);
CREATE INDEX ix_ub_badge ON user_badges(badge_id);
GO
 
/* ---------- password_reset_tokens ---------- */
CREATE TABLE password_reset_tokens (
    id         BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    code_hash  NVARCHAR(150) NOT NULL,
    expires_at DATETIME2(6) NOT NULL,
    attempts   INT          NOT NULL CONSTRAINT df_prt_attempts DEFAULT 0,
    used       BIT          NOT NULL CONSTRAINT df_prt_used DEFAULT 0,
    created_at DATETIME2(6) NOT NULL CONSTRAINT df_prt_created DEFAULT SYSDATETIME(),
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX ix_prt_user ON password_reset_tokens(user_id, used);
GO
 
/* =====================================================================
   DỮ LIỆU MẪU
   (Tài khoản người dùng nên tạo qua ứng dụng để mật khẩu được mã hóa BCrypt)
   ===================================================================== */
 
INSERT INTO badges (code, title, description, icon_class, xp_bonus) VALUES
(N'FIRST_LESSON', N'Bước chân đầu tiên', N'Hoàn thành bài học đầu tiên',      N'fa-shoe-prints', 50),
(N'STREAK_3',     N'Chăm chỉ 3 ngày',    N'Học liên tục 3 ngày',              N'fa-fire',        50),
(N'STREAK_7',     N'Chiến binh 7 ngày',  N'Học liên tục 7 ngày',              N'fa-fire-flame-curved', 100),
(N'QUIZ_MASTER',  N'Bậc thầy trắc nghiệm', N'Đạt 100% trong một bài kiểm tra', N'fa-trophy',       100),
(N'COURSE_DONE',  N'Hoàn thành khóa học', N'Hoàn thành toàn bộ một khóa học', N'fa-graduation-cap', 200);
 
INSERT INTO courses (title, slug, description, level, duration_hours, display_order) VALUES
(N'Tiếng Anh cơ bản A1',   N'beginner-a1',     N'Bảng chữ cái, từ vựng thông dụng', 'BEGINNER_A1',     20, 1),
(N'Giao tiếp sơ cấp A2',   N'elementary-a2',   N'Giao tiếp cơ bản, câu đơn, ngữ pháp nền tảng', 'ELEMENTARY_A2', 25, 2),
(N'Trung cấp B1',          N'intermediate-b1', N'Giao tiếp hàng ngày, đọc hiểu và phản xạ', 'INTERMEDIATE_B1', 30, 3),
(N'Nâng cao B2',           N'advanced-b2',     N'Từ vựng nâng cao, giao tiếp chuyên sâu', 'ADVANCED_B2',  40, 4);
GO

