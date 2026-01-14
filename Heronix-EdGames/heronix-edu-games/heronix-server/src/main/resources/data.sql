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

-- Games are now auto-registered by GameScannerService from game.json files in Heronix-games folder
-- No need for hardcoded game entries here

-- ==================== Game Bundles ====================

-- Standard Bundle (free, included with platform)
MERGE INTO game_bundles (bundle_id, name, description, bundle_type, price, currency, subject_focus, target_grades, icon_url, active, created_at)
KEY(bundle_id) VALUES ('standard-core', 'Core Educational Bundle', 'Essential educational games covering Math, Science, Language Arts, and History. Included free with the Heronix platform.', 'STANDARD', 0.00, 'USD', 'All Subjects', '["3", "4", "5", "6", "7", "8"]', '/api/bundles/standard-core/icon', true, CURRENT_TIMESTAMP);

-- Premium STEM Bundle
MERGE INTO game_bundles (bundle_id, name, description, bundle_type, price, currency, subject_focus, target_grades, icon_url, active, created_at)
KEY(bundle_id) VALUES ('premium-stem', 'Advanced STEM Challenge Pack', 'Advanced games focused on Science, Technology, Engineering, and Mathematics. Includes coding challenges, physics simulations, and advanced math puzzles.', 'PREMIUM', 299.99, 'USD', 'STEM', '["5", "6", "7", "8"]', '/api/bundles/premium-stem/icon', true, CURRENT_TIMESTAMP);

-- Premium Language Pack
MERGE INTO game_bundles (bundle_id, name, description, bundle_type, price, currency, subject_focus, target_grades, icon_url, active, created_at)
KEY(bundle_id) VALUES ('premium-language', 'Language Mastery Pack', 'Comprehensive language arts games including creative writing, grammar challenges, vocabulary builders, and reading comprehension adventures.', 'PREMIUM', 199.99, 'USD', 'Language Arts', '["3", "4", "5", "6"]', '/api/bundles/premium-language/icon', true, CURRENT_TIMESTAMP);

-- Premium World Explorer Bundle
MERGE INTO game_bundles (bundle_id, name, description, bundle_type, price, currency, subject_focus, target_grades, icon_url, active, created_at)
KEY(bundle_id) VALUES ('premium-world', 'World Explorer Pack', 'Journey through geography, world cultures, and global history with interactive maps, virtual tours, and cultural discovery games.', 'PREMIUM', 249.99, 'USD', 'Social Studies', '["4", "5", "6", "7", "8"]', '/api/bundles/premium-world/icon', true, CURRENT_TIMESTAMP);

-- Add games to standard bundle
MERGE INTO bundle_games (bundle_id, game_id) KEY(bundle_id, game_id) VALUES ('standard-core', 'math-quest');
MERGE INTO bundle_games (bundle_id, game_id) KEY(bundle_id, game_id) VALUES ('standard-core', 'spelling-bee');
MERGE INTO bundle_games (bundle_id, game_id) KEY(bundle_id, game_id) VALUES ('standard-core', 'science-lab');
MERGE INTO bundle_games (bundle_id, game_id) KEY(bundle_id, game_id) VALUES ('standard-core', 'history-explorer');

-- ==================== Sample School Licenses ====================

-- Give test school a standard license (free)
MERGE INTO school_licenses (school_id, bundle_id, license_key, license_type, start_date, end_date, max_devices, status, created_at)
KEY(school_id, bundle_id) VALUES ('SCHOOL001', 'standard-core', 'STD-FREE-0001', 'STANDARD', CURRENT_DATE, NULL, NULL, 'ACTIVE', CURRENT_TIMESTAMP);

-- Give test school a trial premium STEM license
MERGE INTO school_licenses (school_id, bundle_id, license_key, license_type, start_date, end_date, max_devices, status, notes, created_at)
KEY(school_id, bundle_id) VALUES ('SCHOOL001', 'premium-stem', 'STEM-TRIAL-2026', 'TRIAL', CURRENT_DATE, DATEADD('DAY', 30, CURRENT_DATE), 50, 'ACTIVE', '30-day trial for evaluation', CURRENT_TIMESTAMP);
