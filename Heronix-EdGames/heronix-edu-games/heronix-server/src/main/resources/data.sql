-- Sample data for testing Heronix Educational Games Server
-- This file is automatically loaded by Spring Boot on startup

-- Create a test teacher (password: teacher123)
-- BCrypt hash of 'teacher123'
MERGE INTO users (username, password_hash, first_name, last_name, email, role, school_id, active, created_at)
KEY(username) VALUES ('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMye1X/vKp7QCXq8kN7z9tHJZkV7yKJ5Lfu',
        'John', 'Doe', 'john.doe@school.edu', 'TEACHER', 'SCHOOL001', true, CURRENT_TIMESTAMP);

-- Create a test admin (password: admin123)
-- BCrypt hash of 'admin123'
MERGE INTO users (username, password_hash, first_name, last_name, email, role, school_id, active, created_at)
KEY(username) VALUES ('admin1', '$2a$10$8K1p/a0dL/Zo3EZ5b4h3ZOcneQh6UcU9Q/nGdQGIVZqNqzYI4zXmS',
        'Jane', 'Smith', 'jane.smith@school.edu', 'ADMIN', 'SCHOOL001', true, CURRENT_TIMESTAMP);

-- Create test students
MERGE INTO students (student_id, first_name, last_initial, grade_level, school_id, active, consent_given, opted_out, created_date)
KEY(student_id) VALUES ('STU001', 'Alice', 'J', '5', 'SCHOOL001', true, true, false, CURRENT_DATE);

MERGE INTO students (student_id, first_name, last_initial, grade_level, school_id, active, consent_given, opted_out, created_date)
KEY(student_id) VALUES ('STU002', 'Bob', 'S', '6', 'SCHOOL001', true, true, false, CURRENT_DATE);

MERGE INTO students (student_id, first_name, last_initial, grade_level, school_id, active, consent_given, opted_out, created_date)
KEY(student_id) VALUES ('STU003', 'Charlie', 'M', '4', 'SCHOOL001', true, true, false, CURRENT_DATE);

-- Create a test registration code
MERGE INTO registration_codes (code, teacher_id, max_uses, times_used, active, created_at)
KEY(code) VALUES ('TEST1234', '1', 10, 0, true, CURRENT_TIMESTAMP);

-- Insert sample educational games
MERGE INTO games (game_id, name, description, version, subject, target_grades, jar_file_name, file_size_bytes, checksum, uploaded_at, active)
KEY(game_id) VALUES ('math-sprint', 'Math Sprint', 'Fast-paced arithmetic practice game with timed challenges', '1.0.0', 'Mathematics', '["3", "4", "5", "6"]', 'math-sprint-1.0.0.jar', 1024000, 'abc123def456', CURRENT_TIMESTAMP, true);

MERGE INTO games (game_id, name, description, version, subject, target_grades, jar_file_name, file_size_bytes, checksum, uploaded_at, active)
KEY(game_id) VALUES ('spelling-bee', 'Spelling Bee', 'Interactive spelling practice with vocabulary building', '1.0.0', 'Language Arts', '["2", "3", "4", "5"]', 'spelling-bee-1.0.0.jar', 856000, 'xyz789uvw456', CURRENT_TIMESTAMP, true);

MERGE INTO games (game_id, name, description, version, subject, target_grades, jar_file_name, file_size_bytes, checksum, uploaded_at, active)
KEY(game_id) VALUES ('science-lab', 'Science Lab', 'Virtual science experiments and interactive learning', '1.0.0', 'Science', '["5", "6", "7", "8"]', 'science-lab-1.0.0.jar', 2048000, 'sci123exp789', CURRENT_TIMESTAMP, true);
