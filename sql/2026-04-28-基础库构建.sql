CREATE DATABASE IF NOT EXISTS universe_scout
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE universe_scout;

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    username VARCHAR(64) NOT NULL COMMENT 'Username',
    password VARCHAR(255) NOT NULL COMMENT 'Password',
    nickname VARCHAR(64) DEFAULT NULL COMMENT 'Nickname',
    role_code VARCHAR(64) NOT NULL DEFAULT 'SUPER_ADMIN' COMMENT 'Role code',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_username (username)
) COMMENT='Admin user';

CREATE TABLE IF NOT EXISTS season (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    season_name VARCHAR(32) NOT NULL COMMENT 'Season name, such as 2021-22',
    start_year INT NOT NULL COMMENT 'Start year',
    end_year INT NOT NULL COMMENT 'End year',
    description VARCHAR(512) DEFAULT NULL COMMENT 'Description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_season_name (season_name)
) COMMENT='Season';

CREATE TABLE IF NOT EXISTS team (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    team_code VARCHAR(32) NOT NULL COMMENT 'Team code, such as HOU or DAL',
    team_name_en VARCHAR(64) NOT NULL COMMENT 'Team English name',
    team_name_cn VARCHAR(64) DEFAULT NULL COMMENT 'Team Chinese name',
    city VARCHAR(64) DEFAULT NULL COMMENT 'City',
    conference VARCHAR(16) DEFAULT NULL COMMENT 'Conference: EAST or WEST',
    division VARCHAR(32) DEFAULT NULL COMMENT 'Division',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_team_code (team_code),
    UNIQUE KEY uk_team_name_en (team_name_en)
) COMMENT='Team';

CREATE TABLE IF NOT EXISTS player (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    player_name VARCHAR(128) NOT NULL COMMENT 'Player English name',
    player_name_cn VARCHAR(128) DEFAULT NULL COMMENT 'Player Chinese name',
    position VARCHAR(16) DEFAULT NULL COMMENT 'Primary position',
    overall_rating INT DEFAULT NULL COMMENT 'Overall rating',
    potential_rating INT DEFAULT NULL COMMENT 'Potential rating',
    remark VARCHAR(512) DEFAULT NULL COMMENT 'Remark',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    KEY idx_player_name (player_name)
) COMMENT='Player';

CREATE TABLE IF NOT EXISTS upload_batch (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    batch_no VARCHAR(64) NOT NULL COMMENT 'Batch number',
    batch_name VARCHAR(128) DEFAULT NULL COMMENT 'Batch name',
    source_type VARCHAR(32) NOT NULL COMMENT 'Source type: ZIP or IMAGE_BATCH',
    season_id BIGINT DEFAULT NULL COMMENT 'Season ID',
    status VARCHAR(32) NOT NULL COMMENT 'Status: UPLOADED, PARSING, PARSED, FAILED',
    total_files INT NOT NULL DEFAULT 0 COMMENT 'Total files',
    success_count INT NOT NULL DEFAULT 0 COMMENT 'Success count',
    failed_count INT NOT NULL DEFAULT 0 COMMENT 'Failed count',
    remark VARCHAR(512) DEFAULT NULL COMMENT 'Remark',
    created_by BIGINT DEFAULT NULL COMMENT 'Creator admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_batch_no (batch_no)
) COMMENT='Upload batch';

CREATE TABLE IF NOT EXISTS upload_file (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    batch_id BIGINT NOT NULL COMMENT 'Upload batch ID',
    original_filename VARCHAR(255) NOT NULL COMMENT 'Original filename',
    file_name VARCHAR(255) NOT NULL COMMENT 'Stored filename',
    file_path VARCHAR(512) NOT NULL COMMENT 'File path',
    file_type VARCHAR(64) DEFAULT NULL COMMENT 'File type',
    file_size BIGINT DEFAULT NULL COMMENT 'File size in bytes',
    image_width INT DEFAULT NULL COMMENT 'Image width',
    image_height INT DEFAULT NULL COMMENT 'Image height',
    status VARCHAR(32) NOT NULL DEFAULT 'UPLOADED' COMMENT 'Status: UPLOADED, PARSED, FAILED',
    error_message TEXT DEFAULT NULL COMMENT 'Error message',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    KEY idx_batch_id (batch_id)
) COMMENT='Upload file';

CREATE TABLE IF NOT EXISTS parse_template (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    template_code VARCHAR(64) NOT NULL COMMENT 'Template code',
    template_name VARCHAR(128) NOT NULL COMMENT 'Template name',
    prompt TEXT NOT NULL COMMENT 'Vision model prompt',
    json_schema JSON DEFAULT NULL COMMENT 'Expected JSON schema',
    status TINYINT NOT NULL DEFAULT 1 COMMENT 'Status: 1 enabled, 0 disabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_template_code (template_code)
) COMMENT='Parse template';

CREATE TABLE IF NOT EXISTS parse_task (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    task_no VARCHAR(64) NOT NULL COMMENT 'Task number',
    batch_id BIGINT NOT NULL COMMENT 'Upload batch ID',
    template_code VARCHAR(64) NOT NULL COMMENT 'Template code',
    status VARCHAR(32) NOT NULL COMMENT 'Status: PENDING, RUNNING, SUCCESS, FAILED, PARTIAL_SUCCESS',
    total_count INT NOT NULL DEFAULT 0 COMMENT 'Total count',
    success_count INT NOT NULL DEFAULT 0 COMMENT 'Success count',
    failed_count INT NOT NULL DEFAULT 0 COMMENT 'Failed count',
    error_message TEXT DEFAULT NULL COMMENT 'Error message',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_batch_id (batch_id),
    KEY idx_template_code (template_code)
) COMMENT='Parse task';

CREATE TABLE IF NOT EXISTS vision_parse_result (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    task_id BIGINT NOT NULL COMMENT 'Parse task ID',
    file_id BIGINT NOT NULL COMMENT 'Image file ID',
    template_code VARCHAR(64) NOT NULL COMMENT 'Template code',
    model_name VARCHAR(128) DEFAULT NULL COMMENT 'Vision model name',
    request_prompt MEDIUMTEXT DEFAULT NULL COMMENT 'Actual request prompt',
    response_raw MEDIUMTEXT DEFAULT NULL COMMENT 'Raw model response',
    response_json JSON DEFAULT NULL COMMENT 'Model response JSON',
    status VARCHAR(32) NOT NULL COMMENT 'Status: SUCCESS, FAILED, NEED_REVIEW',
    error_message TEXT DEFAULT NULL COMMENT 'Error message',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    KEY idx_task_id (task_id),
    KEY idx_file_id (file_id),
    KEY idx_template_code (template_code)
) COMMENT='Vision parse result';

CREATE TABLE IF NOT EXISTS team_lineup_snapshot (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    season_id BIGINT NOT NULL COMMENT 'Season ID',
    snapshot_date DATE DEFAULT NULL COMMENT 'Snapshot date',
    team_id BIGINT NOT NULL COMMENT 'Team ID',
    source_batch_id BIGINT DEFAULT NULL COMMENT 'Source upload batch ID',
    source_file_id BIGINT DEFAULT NULL COMMENT 'Source image file ID',
    status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED' COMMENT 'Status: DRAFT, CONFIRMED',
    remark VARCHAR(512) DEFAULT NULL COMMENT 'Remark',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    KEY idx_season_team (season_id, team_id)
) COMMENT='Team lineup snapshot';

CREATE TABLE IF NOT EXISTS team_lineup_player (
    id BIGINT PRIMARY KEY COMMENT 'Primary key',
    snapshot_id BIGINT NOT NULL COMMENT 'Lineup snapshot ID',
    team_id BIGINT NOT NULL COMMENT 'Team ID',
    position VARCHAR(16) NOT NULL COMMENT 'Position: PG, SG, SF, PF, C',
    player_id BIGINT DEFAULT NULL COMMENT 'Player ID',
    player_name_raw VARCHAR(128) NOT NULL COMMENT 'Raw recognized player name',
    is_confirmed TINYINT NOT NULL DEFAULT 0 COMMENT 'Confirmed: 1 yes, 0 no',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete: 0 no, 1 yes',
    KEY idx_snapshot_id (snapshot_id),
    KEY idx_team_id (team_id),
    KEY idx_player_id (player_id)
) COMMENT='Team lineup player';
