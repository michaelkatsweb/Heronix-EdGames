-- Heronix Educational Games Client Database Schema
-- Local H2 database for offline score queue and device information

-- Device information (single row - one device per installation)
CREATE TABLE IF NOT EXISTS device (
    device_id VARCHAR(255) PRIMARY KEY,
    device_name VARCHAR(255) NOT NULL,
    device_type VARCHAR(50),
    os_name VARCHAR(100),
    os_version VARCHAR(100),
    app_version VARCHAR(50),
    status VARCHAR(50), -- PENDING, APPROVED, REJECTED, REVOKED
    registration_code VARCHAR(50),
    student_id VARCHAR(255),
    registered_at TIMESTAMP,
    approved_at TIMESTAMP,
    last_sync_at TIMESTAMP,
    jwt_token TEXT,
    token_expires_at TIMESTAMP
);

-- Student info (cached from server)
CREATE TABLE IF NOT EXISTS student (
    student_id VARCHAR(255) PRIMARY KEY,
    first_name VARCHAR(100),
    last_initial VARCHAR(1),
    grade_level VARCHAR(10),
    school_id VARCHAR(100),
    active BOOLEAN,
    consent_given BOOLEAN,
    synced_at TIMESTAMP
);

-- Game scores (offline queue)
CREATE TABLE IF NOT EXISTS game_score (
    score_id VARCHAR(255) PRIMARY KEY,
    student_id VARCHAR(255) NOT NULL,
    game_id VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    max_score INTEGER NOT NULL,
    time_seconds INTEGER,
    correct_answers INTEGER,
    incorrect_answers INTEGER,
    completion_percentage INTEGER,
    completed BOOLEAN,
    difficulty_level VARCHAR(50),
    played_at TIMESTAMP NOT NULL,
    device_id VARCHAR(255),
    synced BOOLEAN DEFAULT FALSE,
    synced_at TIMESTAMP,
    sync_attempts INTEGER DEFAULT 0,
    last_sync_error TEXT,
    metadata TEXT -- JSON for additional game-specific data
);

-- Installed games metadata
CREATE TABLE IF NOT EXISTS installed_game (
    game_id VARCHAR(255) PRIMARY KEY,
    game_name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(50),
    subject VARCHAR(100),
    target_grades TEXT, -- JSON array
    jar_path VARCHAR(500),
    jar_checksum VARCHAR(64), -- SHA-256
    installed_at TIMESTAMP,
    last_played_at TIMESTAMP,
    file_size_bytes BIGINT
);

-- Sync history/audit
CREATE TABLE IF NOT EXISTS sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_started_at TIMESTAMP NOT NULL,
    sync_completed_at TIMESTAMP,
    scores_uploaded INTEGER DEFAULT 0,
    scores_failed INTEGER DEFAULT 0,
    success BOOLEAN,
    error_message TEXT
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_score_synced ON game_score(synced, played_at);
CREATE INDEX IF NOT EXISTS idx_score_student ON game_score(student_id, played_at DESC);
CREATE INDEX IF NOT EXISTS idx_score_game ON game_score(game_id, played_at DESC);
CREATE INDEX IF NOT EXISTS idx_sync_log_time ON sync_log(sync_started_at DESC);
