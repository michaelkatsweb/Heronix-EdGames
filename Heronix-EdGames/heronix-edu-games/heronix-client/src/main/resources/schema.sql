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

-- Game scores (offline queue with version tracking for delta sync)
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
    metadata TEXT, -- JSON for additional game-specific data
    -- Delta sync fields
    local_version INTEGER DEFAULT 1,     -- Increments on each local update
    server_version INTEGER DEFAULT 0,    -- Last known server version
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    content_hash VARCHAR(64),            -- SHA-256 of content for change detection
    sync_status VARCHAR(20) DEFAULT 'PENDING' -- PENDING, SYNCED, CONFLICT, ERROR
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

-- Sync history/audit (enhanced with delta sync tracking)
CREATE TABLE IF NOT EXISTS sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sync_started_at TIMESTAMP NOT NULL,
    sync_completed_at TIMESTAMP,
    sync_type VARCHAR(50) DEFAULT 'FULL', -- FULL, DELTA, MANUAL
    scores_uploaded INTEGER DEFAULT 0,
    scores_failed INTEGER DEFAULT 0,
    bytes_transferred BIGINT DEFAULT 0,
    success BOOLEAN,
    error_message TEXT,
    conflicts_detected INTEGER DEFAULT 0,
    conflicts_resolved INTEGER DEFAULT 0
);

-- Sync conflicts tracking
CREATE TABLE IF NOT EXISTS sync_conflict (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,  -- 'game_score', 'student', etc.
    entity_id VARCHAR(255) NOT NULL,
    field_name VARCHAR(100),           -- Which field has conflict (null = entire entity)
    local_value TEXT,
    server_value TEXT,
    local_version INTEGER,
    server_version INTEGER,
    local_timestamp TIMESTAMP,
    server_timestamp TIMESTAMP,
    conflict_type VARCHAR(50) NOT NULL, -- UPDATE_CONFLICT, DELETE_CONFLICT, VERSION_MISMATCH
    resolution VARCHAR(50),            -- KEEP_LOCAL, KEEP_SERVER, MERGE, PENDING
    resolved_at TIMESTAMP,
    resolved_by VARCHAR(100),          -- 'auto', 'user', 'server_wins'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata TEXT                      -- JSON for additional context
);

-- Delta sync checkpoint (tracks last successful sync point)
CREATE TABLE IF NOT EXISTS sync_checkpoint (
    id VARCHAR(50) PRIMARY KEY,        -- 'game_score', 'student', etc.
    last_sync_timestamp TIMESTAMP,
    last_sync_version BIGINT DEFAULT 0,
    server_sequence_id VARCHAR(255),   -- Server's sequence marker
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_score_synced ON game_score(synced, played_at);
CREATE INDEX IF NOT EXISTS idx_score_student ON game_score(student_id, played_at DESC);
CREATE INDEX IF NOT EXISTS idx_score_game ON game_score(game_id, played_at DESC);
CREATE INDEX IF NOT EXISTS idx_sync_log_time ON sync_log(sync_started_at DESC);
CREATE INDEX IF NOT EXISTS idx_sync_conflict_pending ON sync_conflict(resolution, created_at);
CREATE INDEX IF NOT EXISTS idx_sync_conflict_entity ON sync_conflict(entity_type, entity_id);
