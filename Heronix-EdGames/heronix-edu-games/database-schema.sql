-- Heronix Educational Games Platform - Database Schema
-- This schema works with H2 database (embedded)

-- Create tables

-- Schools table
CREATE TABLE IF NOT EXISTS schools (
    school_id VARCHAR(50) PRIMARY KEY,
    school_name VARCHAR(200) NOT NULL,
    district VARCHAR(200),
    state VARCHAR(2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- Students table (FERPA compliant - minimal PII)
CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(50) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_initial VARCHAR(1) NOT NULL,
    grade_level VARCHAR(10),
    school_id VARCHAR(50),
    consent_given BOOLEAN DEFAULT FALSE,
    consent_date DATE,
    opted_out BOOLEAN DEFAULT FALSE,
    opt_out_date DATE,
    active BOOLEAN DEFAULT TRUE,
    created_date DATE DEFAULT CURRENT_DATE,
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);

-- Create index on school_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_students_school ON students(school_id);

-- Teachers/Users table
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(200),
    role VARCHAR(20) NOT NULL, -- TEACHER, ADMIN, IT_ADMIN
    school_id VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);

CREATE INDEX IF NOT EXISTS idx_users_school ON users(school_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Classes table
CREATE TABLE IF NOT EXISTS classes (
    class_id VARCHAR(50) PRIMARY KEY,
    class_name VARCHAR(200) NOT NULL,
    teacher_id VARCHAR(50) NOT NULL,
    school_id VARCHAR(50) NOT NULL,
    grade_level VARCHAR(10),
    subject VARCHAR(100),
    school_year VARCHAR(20),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES users(user_id),
    FOREIGN KEY (school_id) REFERENCES schools(school_id)
);

CREATE INDEX IF NOT EXISTS idx_classes_teacher ON classes(teacher_id);
CREATE INDEX IF NOT EXISTS idx_classes_school ON classes(school_id);

-- Class enrollment (many-to-many relationship)
CREATE TABLE IF NOT EXISTS class_enrollment (
    enrollment_id VARCHAR(50) PRIMARY KEY,
    class_id VARCHAR(50) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    enrolled_date DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, WITHDRAWN, COMPLETED
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    UNIQUE(class_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_enrollment_class ON class_enrollment(class_id);
CREATE INDEX IF NOT EXISTS idx_enrollment_student ON class_enrollment(student_id);

-- Devices table
CREATE TABLE IF NOT EXISTS devices (
    device_id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    device_name VARCHAR(200),
    device_type VARCHAR(20), -- DESKTOP, LAPTOP, MOBILE, TABLET
    operating_system VARCHAR(50),
    os_version VARCHAR(50),
    app_version VARCHAR(20),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, REVOKED
    registration_code VARCHAR(50),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by VARCHAR(50),
    last_sync_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    deactivation_reason VARCHAR(500),
    auth_token VARCHAR(255),
    token_expires_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_devices_student ON devices(student_id);
CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status);

-- Games table
CREATE TABLE IF NOT EXISTS games (
    game_id VARCHAR(50) PRIMARY KEY,
    game_name VARCHAR(200) NOT NULL,
    description TEXT,
    subject VARCHAR(100),
    version VARCHAR(20),
    min_grade VARCHAR(10),
    max_grade VARCHAR(10),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Game scores table (education records under FERPA)
CREATE TABLE IF NOT EXISTS game_scores (
    score_id VARCHAR(50) PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL,
    game_id VARCHAR(50) NOT NULL,
    score INT NOT NULL DEFAULT 0,
    max_score INT NOT NULL DEFAULT 100,
    correct_answers INT DEFAULT 0,
    incorrect_answers INT DEFAULT 0,
    time_seconds INT DEFAULT 0,
    completion_percentage INT DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    difficulty_level VARCHAR(20),
    played_at TIMESTAMP NOT NULL,
    device_id VARCHAR(50),
    synced BOOLEAN DEFAULT FALSE,
    synced_at TIMESTAMP,
    metadata TEXT, -- JSON string for additional data
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (game_id) REFERENCES games(game_id),
    FOREIGN KEY (device_id) REFERENCES devices(device_id)
);

CREATE INDEX IF NOT EXISTS idx_scores_student ON game_scores(student_id);
CREATE INDEX IF NOT EXISTS idx_scores_game ON game_scores(game_id);
CREATE INDEX IF NOT EXISTS idx_scores_played ON game_scores(played_at);
CREATE INDEX IF NOT EXISTS idx_scores_synced ON game_scores(synced);

-- Audit log table (for FERPA compliance)
CREATE TABLE IF NOT EXISTS audit_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(50),
    action VARCHAR(100) NOT NULL, -- ACCESS, MODIFY, DELETE, EXPORT, etc.
    entity_type VARCHAR(50), -- STUDENT, SCORE, DEVICE, etc.
    entity_id VARCHAR(50),
    details TEXT,
    ip_address VARCHAR(50),
    result VARCHAR(20) -- SUCCESS, FAILURE, DENIED
);

CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity ON audit_log(entity_type, entity_id);

-- Registration codes table (for device registration)
CREATE TABLE IF NOT EXISTS registration_codes (
    code VARCHAR(50) PRIMARY KEY,
    class_id VARCHAR(50),
    teacher_id VARCHAR(50) NOT NULL,
    max_uses INT DEFAULT 1,
    times_used INT DEFAULT 0,
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_regcodes_teacher ON registration_codes(teacher_id);

-- Game assignments table (teacher assigns games to students/classes)
CREATE TABLE IF NOT EXISTS game_assignments (
    assignment_id VARCHAR(50) PRIMARY KEY,
    game_id VARCHAR(50) NOT NULL,
    class_id VARCHAR(50),
    student_id VARCHAR(50),
    teacher_id VARCHAR(50) NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    min_score INT,
    required_completion BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (game_id) REFERENCES games(game_id),
    FOREIGN KEY (class_id) REFERENCES classes(class_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id),
    FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_assignments_game ON game_assignments(game_id);
CREATE INDEX IF NOT EXISTS idx_assignments_class ON game_assignments(class_id);
CREATE INDEX IF NOT EXISTS idx_assignments_student ON game_assignments(student_id);

-- Insert sample data for testing

-- Sample school
INSERT INTO schools (school_id, school_name, district, state) 
VALUES ('SCHOOL001', 'Sample Elementary School', 'Sample District', 'CA');

-- Sample games
INSERT INTO games (game_id, game_name, description, subject, version, min_grade, max_grade) VALUES
('math-sprint', 'Math Sprint', 'Solve math problems as quickly as you can', 'Mathematics', '1.0.0', '2', '6'),
('word-builder', 'Word Builder', 'Build words from letters to improve vocabulary', 'Language Arts', '1.0.0', '1', '5'),
('science-quest', 'Science Quest', 'Explore scientific concepts through interactive challenges', 'Science', '1.0.0', '3', '8'),
('geography-challenge', 'Geography Challenge', 'Test your knowledge of world geography', 'Geography', '1.0.0', '4', '8'),
('history-timeline', 'History Timeline', 'Place historical events in chronological order', 'History', '1.0.0', '5', '12');

-- Sample teacher (password: 'teacher123' - in production use proper hashing)
INSERT INTO users (user_id, username, password_hash, first_name, last_name, email, role, school_id) 
VALUES ('TEACHER001', 'teacher1', 'CHANGEME', 'Jane', 'Smith', 'jane.smith@school.edu', 'TEACHER', 'SCHOOL001');

-- Sample class
INSERT INTO classes (class_id, class_name, teacher_id, school_id, grade_level, subject, school_year) 
VALUES ('CLASS001', 'Math 3A', 'TEACHER001', 'SCHOOL001', '3', 'Mathematics', '2025-2026');

-- Sample students
INSERT INTO students (student_id, first_name, last_initial, grade_level, school_id, consent_given) VALUES
('STUDENT001', 'Alex', 'J', '3', 'SCHOOL001', TRUE),
('STUDENT002', 'Maria', 'G', '3', 'SCHOOL001', TRUE),
('STUDENT003', 'James', 'L', '3', 'SCHOOL001', TRUE);

-- Enroll students in class
INSERT INTO class_enrollment (enrollment_id, class_id, student_id) VALUES
('ENROLL001', 'CLASS001', 'STUDENT001'),
('ENROLL002', 'CLASS001', 'STUDENT002'),
('ENROLL003', 'CLASS001', 'STUDENT003');

-- Views for common queries

-- Student performance summary
CREATE OR REPLACE VIEW student_performance AS
SELECT 
    s.student_id,
    s.first_name,
    s.last_initial,
    s.grade_level,
    g.game_name,
    COUNT(gs.score_id) as times_played,
    AVG(gs.score) as avg_score,
    MAX(gs.score) as best_score,
    AVG(gs.completion_percentage) as avg_completion,
    SUM(CASE WHEN gs.completed THEN 1 ELSE 0 END) as times_completed,
    AVG(gs.correct_answers * 100.0 / NULLIF(gs.correct_answers + gs.incorrect_answers, 0)) as avg_accuracy
FROM students s
LEFT JOIN game_scores gs ON s.student_id = gs.student_id
LEFT JOIN games g ON gs.game_id = g.game_id
GROUP BY s.student_id, s.first_name, s.last_initial, s.grade_level, g.game_name;

-- Class performance summary
CREATE OR REPLACE VIEW class_performance AS
SELECT 
    c.class_id,
    c.class_name,
    c.teacher_id,
    u.first_name as teacher_first_name,
    u.last_name as teacher_last_name,
    COUNT(DISTINCT ce.student_id) as total_students,
    COUNT(DISTINCT gs.score_id) as total_game_sessions,
    AVG(gs.score) as avg_class_score,
    AVG(gs.correct_answers * 100.0 / NULLIF(gs.correct_answers + gs.incorrect_answers, 0)) as avg_class_accuracy
FROM classes c
JOIN users u ON c.teacher_id = u.user_id
LEFT JOIN class_enrollment ce ON c.class_id = ce.class_id AND ce.status = 'ACTIVE'
LEFT JOIN game_scores gs ON ce.student_id = gs.student_id
GROUP BY c.class_id, c.class_name, c.teacher_id, u.first_name, u.last_name;

-- Pending device approvals
CREATE OR REPLACE VIEW pending_devices AS
SELECT 
    d.device_id,
    d.device_name,
    d.device_type,
    d.registered_at,
    s.first_name,
    s.last_initial,
    s.grade_level,
    ce.class_id,
    c.class_name,
    c.teacher_id
FROM devices d
JOIN students s ON d.student_id = s.student_id
LEFT JOIN class_enrollment ce ON s.student_id = ce.student_id AND ce.status = 'ACTIVE'
LEFT JOIN classes c ON ce.class_id = c.class_id
WHERE d.status = 'PENDING'
ORDER BY d.registered_at DESC;
